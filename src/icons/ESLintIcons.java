package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.Icon;

public class ESLintIcons {
    private static Icon load(String path) {
        return IconLoader.getIcon(path, ESLintIcons.class);
    }

//    public static class Library {
//        public static final Icon JsCompact = ESLintIcons.load("/icons/library/jsCompact.png");
//        public static final Icon JsLibrary = ESLintIcons.load("/icons/library/jsLibrary.png");
//        public static final Icon JsSource = ESLintIcons.load("/icons/library/jsSource.png");
//    }
//
//    public static class Grunt {
//        public static final Icon Grunt = ESLintIcons.load("/icons/grunt/grunt.png");
//        public static final Icon Grunt_force_option = ESLintIcons.load("/icons/grunt/grunt_force_option.png");
//        public static final Icon Grunt_toolwindow = ESLintIcons.load("/icons/grunt/grunt_toolwindow.png");
//        public static final Icon Grunt_verbose_option = ESLintIcons.load("/icons/grunt/grunt_verbose_option.png");
//    }

    public static final class FileTypes {
        private FileTypes() {
        }
        public static final Icon ESLint = ESLintIcons.load("/icons/fileTypes/eslint.png");
//        public static final Icon JsHint = ESLintIcons.load("/icons/fileTypes/jsHint.png");
//        public static final Icon JsTestFile = ESLintIcons.load("/icons/fileTypes/jsTestFile.png");
    }
}
