package net.argius.eclipse.proj_gen;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public final class Projects {

    public static void createProject(ProjectType type, Map<String, Object> ctx) {
        Project project;
        switch (type) {
            case Plain:
                project = new PlainProject();
                break;
            case Java:
                project = new JavaProject();
                break;
            case Maven2:
                project = new Maven2Project();
                break;
            case Unknown:
            default:
                return;
        }
        final boolean usingJava = type != ProjectType.Plain;
        if (usingJava) {
            addOptionalJavaFiles(type, ctx, project);
        }
        project.create(ctx);
    }

    private static void addOptionalJavaFiles(ProjectType type, Map<String, Object> ctx, Project project) {
        final boolean isMaven2Project = type == ProjectType.Maven2;
        final String javaDir = isMaven2Project ? "src/main/java" : "src";
        final String resDir = isMaven2Project ? "src/main/resources" : "src";
        final String packageName = String.valueOf(ctx.get(PropKeys.rootPackage));
        final String javaSubDir = javaDir + "/" + packageName + "/";
        final String resSubDir = resDir + "/" + packageName + "/";
        if (isTrue(ctx, PropKeys.createWebAppFiles)) {
            final String webappDir = "src/main/webapp/";
            project.addFileGen(webappDir + "WEB-INF/web.xml", "war-web.xml");
            project.addFileGen(webappDir + "index.jsp", "war-index.jsp");
            project.addFileGen(javaSubDir + "Servlet1.java", "war-Servlet1.java");
            project.addFileGen(".settings/org.eclipse.wst.common.component", "war-wst.common.component");
            project.addFileGen(".settings/org.eclipse.wst.common.project.facet.core.xml", "war-project.facet.core.xml");
        }
        else if (isTrue(ctx, PropKeys.createJavaFX8Files)) {
            project.addFileGen(javaSubDir + "App.java", "javafx-App.java");
            project.addFileGen(resSubDir + "main.fxml", "javafx-main.fxml");
        }
        else {
            project.addFileGen(javaSubDir + "App.java", "java-App.java");
        }
        if (isTrue(ctx, PropKeys.createAntFile)) {
            project.addFileGen("build.xml", "ant-build.xml");
            project.addFileGen(javaDir + "/MANIFEST.MF", "ant-MANIFEST.MF");
        }
    }

    private static boolean isTrue(Map<String, Object> m, String key) {
        Object o = m.get(key);
        if (o instanceof Boolean) {
            return (boolean)o;
        }
        return false;
    }

}

final class PlainProject extends Project {

    PlainProject() {
        addFileGen(".project", "project");
    }

}

final class JavaProject extends Project {

    JavaProject() {
        addContextChange(PropKeys.usingJava, true);
        addFileGen(".project", "project");
        addFileGen(".classpath", "classpath");
        addFileGen(".gitignore", "gitignore");
        addFileGen(".settings/org.eclipse.jdt.core.prefs", "org.eclipse.jdt.core.prefs");
        addDirGen("src");
    }

}

final class Maven2Project extends Project {

    public Maven2Project() {
        addContextChange(PropKeys.usingJava, true);
        addContextChange(PropKeys.usingMaven2, true);
        addFileGen(".project", "project");
        addFileGen(".classpath", "classpath");
        addFileGen(".gitignore", "gitignore");
        addFileGen(".settings/org.eclipse.jdt.core.prefs", "org.eclipse.jdt.core.prefs");
        addFileGen("pom.xml", "pom.xml");
        addDirGen("src/main/java");
        addDirGen("src/test/java");
    }

}

abstract class Project {

    List<GenTask> tasks = new ArrayList<>();

    void create(Map<String, Object> ctx) {
        final Path rootDir = Paths.get(ctx.get(PropKeys.dir).toString());
        VelocityEngine ve = new VelocityEngine();
        ve.init(Utils.getPropertiesFromResource(Project.class, "velocity.properties"));
        VelocityContext vc = new VelocityContext(ctx);
        for (GenTask task : tasks) {
            if (task instanceof FileGenTask) {
                ((FileGenTask)task).generate(rootDir, ve, vc);
            }
            else if (task instanceof DirGenTask) {
                // TODO check boolean return value
                ((DirGenTask)task).make(rootDir);
            }
            else if (task instanceof ContextChangeTask) {
                ((ContextChangeTask)task).change(vc);
            }
            else {
                throw new AssertionError("task class = " + task.getClass());
            }
        }
    }

    FileGenTask addFileGen(String relPath, String templateId) {
        FileGenTask task = new FileGenTask();
        task.relPath = relPath;
        task.templateId = templateId;
        tasks.add(task);
        return task;
    }

    DirGenTask addDirGen(String relPath) {
        DirGenTask task = new DirGenTask();
        task.relPath = relPath;
        tasks.add(task);
        return task;
    }

    ContextChangeTask addContextChange(String key, Object value) {
        ContextChangeTask task = new ContextChangeTask();
        task.key = key;
        task.value = value;
        tasks.add(task);
        return task;
    }

}
