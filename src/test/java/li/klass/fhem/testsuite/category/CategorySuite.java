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

package li.klass.fhem.testsuite.category;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import org.junit.experimental.categories.*;
import org.junit.runner.Description;
import org.junit.runner.manipulation.*;
import org.junit.runners.Suite;
import org.junit.runners.model.*;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;

public class CategorySuite extends Suite {

    public static class CategoryFilter extends Filter {
        public static CategoryFilter include(Class<?> categoryType) {
            return new CategoryFilter(categoryType, null);
        }

        private final Class<?> fIncluded;

        private final Class<?> fExcluded;

        public CategoryFilter(Class<?> includedCategory,
                              Class<?> excludedCategory) {
            fIncluded = includedCategory;
            fExcluded = excludedCategory;
        }

        @Override
        public String describe() {
            return "category " + fIncluded;
        }

        @Override
        public boolean shouldRun(Description description) {
            if (hasCorrectCategoryAnnotation(description))
                return true;
            for (Description each : description.getChildren())
                if (shouldRun(each))
                    return true;
            return false;
        }

        private boolean hasCorrectCategoryAnnotation(Description description) {
            List<Class<?>> categories = categories(description);
            if (categories.isEmpty())
                return fIncluded == null;
            for (Class<?> each : categories)
                if (fExcluded != null && fExcluded.isAssignableFrom(each))
                    return false;
            for (Class<?> each : categories)
                if (fIncluded == null || fIncluded.isAssignableFrom(each))
                    return true;
            return false;
        }

        private List<Class<?>> categories(Description description) {
            ArrayList<Class<?>> categories = newArrayList();
            categories.addAll(Arrays.asList(directCategories(description)));
            categories.addAll(Arrays.asList(directCategories(parentDescription(description.getTestClass()))));
            return categories;
        }

        private Description parentDescription(Class<?> testClass) {
            if (testClass == null)
                return null;

            if (!testClass.isAnnotationPresent(Category.class) && testClass.getSuperclass() != null) {
                return parentDescription(testClass.getSuperclass());
            }

            return Description.createSuiteDescription(testClass);
        }

        private Class<?>[] directCategories(Description description) {
            if (description == null)
                return new Class<?>[0];
            Category annotation = description.getAnnotation(Category.class);
            if (annotation == null)
                return new Class<?>[0];
            return annotation.value();
        }
    }

    public CategorySuite(Class<?> cls, RunnerBuilder builder) throws InitializationError {
        super(builder, cls, getSuiteClasses());

        try {
            filter(new CategoryFilter(getIncludedCategory(cls),
                    getExcludedCategory(cls)));
        } catch (NoTestsRemainException e) {
            throw new InitializationError(e);
        }
    }

    private Class<?> getIncludedCategory(Class<?> cls) {
        Categories.IncludeCategory annotation = cls.getAnnotation(Categories.IncludeCategory.class);
        return annotation == null ? null : annotation.value()[0];
    }

    private Class<?> getExcludedCategory(Class<?> cls) {
        Categories.ExcludeCategory annotation = cls.getAnnotation(Categories.ExcludeCategory.class);
        return annotation == null ? null : annotation.value()[0];
    }

    private static Class[] getSuiteClasses() {
        final String basePath = getBasePath();
        File basePathFile = new File(basePath);

        ImmutableList<Class<?>> classes = Files.fileTreeTraverser().breadthFirstTraversal(basePathFile).filter(input -> input.getName().endsWith(".java")).transform((Function<File, Class<?>>) input -> toClass(input, basePath)).filter(input -> input != null && !Modifier.isAbstract(input.getModifiers())).toList();
        return (classes.toArray(new Class[classes.size()]));

    }

    protected static Class<?> toClass(File file, String basePath) {
        try {
            return Class.forName(toClassName(file, basePath));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    protected static String toClassName(File file, String basePath) {
        String filePath = file.getAbsolutePath();
        filePath = filePath.substring(basePath.length());
        filePath = filePath.replace("/src/testFunctionalityIsSetOnAllDevices/java/", "");
        filePath = filePath.replace(".java", "");
        filePath = filePath.replaceAll("/", ".");

        return filePath;
    }

    private static String getBasePath() {
        String absolutePath = new File("").getAbsolutePath();
        if (!absolutePath.endsWith("tests")) {
            absolutePath = absolutePath + "/tests";
        }

        return absolutePath;
    }
}
