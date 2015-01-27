package net.argius.eclipse.proj_gen.templates;

public final class Templates {

    private static final String packagePath = Templates.class.getPackage().getName().replace('.', '/');

    private Templates() {
        //
    }

    public static String resolveTemplateResourceName(String templateName) {
        return packagePath + "/" + templateName + ".vm";
    }

}
