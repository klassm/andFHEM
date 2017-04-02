package li.klass.fhem.service.device;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import li.klass.fhem.domain.log.ConcernsDevicePredicate;
import li.klass.fhem.service.graph.gplot.GPlotDefinition;
import li.klass.fhem.service.graph.gplot.GPlotHolder;
import li.klass.fhem.service.graph.gplot.SvgGraphDefinition;
import li.klass.fhem.service.room.RoomListService;
import li.klass.fhem.service.room.xmllist.XmlListDevice;

import static com.google.common.collect.FluentIterable.from;
import static li.klass.fhem.service.room.xmllist.XmlListDevice.hasName;

public class GraphDefinitionsForDeviceService {

    private final RoomListService roomListService;
    private final GPlotHolder gPlotHolder;

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphDefinitionsForDeviceService.class);

    @Inject
    GraphDefinitionsForDeviceService(RoomListService roomListService, GPlotHolder gPlotHolder) {
        this.roomListService = roomListService;
        this.gPlotHolder = gPlotHolder;
    }

    public ImmutableSet<SvgGraphDefinition> graphDefinitionsFor(Context context, XmlListDevice device, Optional<String> connectionId) {
        final ImmutableSet<XmlListDevice> allDevices = roomListService.getAllRoomsDeviceList(connectionId, context)
                .getAllDevicesAsXmllistDevice();

        LOGGER.info("graphDefinitionsFor(name={},connection={})", device.getName(), connectionId.or("--"));
        ImmutableSet<SvgGraphDefinition> devices = from(allDevices)
                .filter(XmlListDevice.hasType("SVG"))
                .filter(hasConcerningLogDevice(allDevices, device))
                .filter(gplotDefinitionExists(allDevices))
                .transform(toGraphDefinition(allDevices)).toSet();
        for (SvgGraphDefinition svgGraphDefinition : devices) {
            LOGGER.info("graphDefinitionsFor(name={},connection={}) - found SVG with name {}", device.getName(), connectionId.or("--"), svgGraphDefinition.getName());
        }
        return devices;
    }

    @NonNull
    private Function<XmlListDevice, SvgGraphDefinition> toGraphDefinition(final ImmutableSet<XmlListDevice> allDevices) {
        return new Function<XmlListDevice, SvgGraphDefinition>() {
            @Override
            public SvgGraphDefinition apply(XmlListDevice svgDevice) {
                String logDeviceName = svgDevice.getInternal("LOGDEVICE").get();
                String gplotFileName = svgDevice.getInternal("GPLOTFILE").get();
                GPlotDefinition gPlotDefinition = gPlotHolder.definitionFor(gplotFileName, isConfigDb(allDevices)).get();

                List<String> labels = Arrays.asList(svgDevice.getAttribute("label")
                        .or("").replaceAll("\"", "").split(","));
                String title = svgDevice.getAttribute("title").or("");
                List<String> plotfunction = Arrays.asList(svgDevice.getAttribute("plotfunction").or("").trim().split(" "));

                return new SvgGraphDefinition(svgDevice.getName(), gPlotDefinition, logDeviceName, labels, title, plotfunction);
            }
        };
    }

    @NonNull
    private Predicate<XmlListDevice> gplotDefinitionExists(final ImmutableSet<XmlListDevice> allDevices) {
        return new Predicate<XmlListDevice>() {
            @Override
            public boolean apply(XmlListDevice input) {
                if (input == null) {
                    return false;
                }
                Optional<String> gplotFileName = input.getInternal("GPLOTFILE");
                return gplotFileName.isPresent()
                        && gPlotHolder.definitionFor(gplotFileName.get(), isConfigDb(allDevices)).isPresent();
            }
        };
    }

    @NonNull
    private Predicate<XmlListDevice> hasConcerningLogDevice(final ImmutableSet<XmlListDevice> allDevices, final XmlListDevice inputDevice) {
        return new Predicate<XmlListDevice>() {
            @Override
            public boolean apply(XmlListDevice svgDevice) {
                if (svgDevice == null) {
                    return false;
                }
                final Optional<String> logDeviceName = svgDevice.getInternal("LOGDEVICE");
                if (!logDeviceName.isPresent()) {
                    return false;
                }

                Optional<XmlListDevice> logDevice = from(allDevices)
                        .firstMatch(hasName(logDeviceName.get()));
                if (!logDevice.isPresent()) {
                    return false;
                }
                Optional<String> logDeviceRegexp = logDevice.get().getInternal("REGEXP");
                return logDeviceRegexp.isPresent() &&
                        ConcernsDevicePredicate.forPattern(logDeviceRegexp.get()).apply(inputDevice);
            }
        };
    }

    private boolean isConfigDb(ImmutableSet<XmlListDevice> allDevices) {
        return from(allDevices)
                .anyMatch(new Predicate<XmlListDevice>() {
                    @Override
                    public boolean apply(XmlListDevice input) {
                        return input != null && "configDB".equals(input.getAttribute("configfile").orNull());
                    }
                });
    }
}
