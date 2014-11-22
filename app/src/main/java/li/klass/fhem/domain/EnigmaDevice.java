/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.domain;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.ToggleableDevice;
import li.klass.fhem.domain.core.XmllistAttribute;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.ValueExtractUtil;

public class EnigmaDevice extends ToggleableDevice<EnigmaDevice> {
    @XmllistAttribute("channel")
    @ShowField(description = ResourceIdMapper.channel)
    private String channel;

    @XmllistAttribute("currentTitle")
    @ShowField(description = ResourceIdMapper.currentTitle)
    private String currentTitle;

    @XmllistAttribute("power")
    private String power;

    @XmllistAttribute("servicename")
    @ShowField(description = ResourceIdMapper.serviceName)
    private String serviceName;

    @XmllistAttribute("eventduration_hr")
    @ShowField(description = ResourceIdMapper.event_duration_current_title)
    private String eventDurationHour;

    @XmllistAttribute("eventduration_next_hr")
    @ShowField(description = ResourceIdMapper.event_duration_next_title)
    private String eventDurationNextHour;

    @XmllistAttribute("eventname_next")
    @ShowField(description = ResourceIdMapper.event_name_next_title)
    private String eventNameNext;

    @XmllistAttribute("eventremaining_hr")
    @ShowField(description = ResourceIdMapper.event_rest_current_title)
    private String eventRemainingHour;

    @XmllistAttribute("eventstart")
    @ShowField(description = ResourceIdMapper.event_starting_time)
    private String eventStart;

    @XmllistAttribute("eventstart_next_hr")
    @ShowField(description = ResourceIdMapper.event_starting_time_next_title)
    private String eventStartNextHour;

    @XmllistAttribute("hdd1_capacity")
    @ShowField(description = ResourceIdMapper.hdd_capacity)
    private String hdd1Capacity;

    @XmllistAttribute("hdd1_free")
    @ShowField(description = ResourceIdMapper.hdd_free)
    private String hdd1Free;

    @XmllistAttribute("input")
    private String input;

    @XmllistAttribute("lanmac")
    @ShowField(description = ResourceIdMapper.mac)
    private String lanmac;

    @XmllistAttribute("model")
    @ShowField(description = ResourceIdMapper.model)
    private String model;

    @XmllistAttribute("mute")
    private String mute;

    @XmllistAttribute("servicevideosize")
    @ShowField(description = ResourceIdMapper.video_size)
    private String videoSize;

    @XmllistAttribute("volume")
    @ShowField(description = ResourceIdMapper.musicVolume)
    private String volume;

    @Override
    public DeviceFunctionality getDeviceGroup() {
        return DeviceFunctionality.REMOTE_CONTROL;
    }

    @Override
    public boolean isOnByState() {
        return "on".equalsIgnoreCase(power);
    }

    @Override
    public void setState(String state) {
        super.setState(state);
        if (state.equalsIgnoreCase("on") || state.equalsIgnoreCase("off")) {
            power = state;
        }
    }

    public String getChannel() {
        return channel;
    }

    public String getCurrentTitle() {
        return currentTitle;
    }

    public String getPower() {
        return power;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getEventDurationHour() {
        return eventDurationHour;
    }

    public String getEventDurationNextHour() {
        return eventDurationNextHour;
    }

    public String getEventNameNext() {
        return eventNameNext;
    }

    public String getEventRemainingHour() {
        return eventRemainingHour;
    }

    public String getEventStart() {
        return eventStart;
    }

    public String getEventStartNextHour() {
        return eventStartNextHour;
    }

    public String getHdd1Capacity() {
        return hdd1Capacity;
    }

    public String getHdd1Free() {
        return hdd1Free;
    }

    public String getInput() {
        return input;
    }

    public String getLanmac() {
        return lanmac;
    }

    public String getModel() {
        return model;
    }

    public String getMute() {
        return mute;
    }

    public String getVideoSize() {
        return videoSize;
    }

    public String getVolume() {
        return volume;
    }

    public boolean isMuted() {
        return "on".equalsIgnoreCase(mute);
    }

    public int getVolumeProgress() {
        return ValueExtractUtil.extractLeadingInt(volume);
    }
}
