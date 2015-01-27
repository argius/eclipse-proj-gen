package net.argius.eclipse.proj_gen;

import java.io.*;
import java.nio.file.Path;
import net.argius.eclipse.proj_gen.templates.Templates;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

abstract class GenTask {

    String relPath;

}

class FileGenTask extends GenTask {

    String templateId;

    boolean generate(Path rootDir, VelocityEngine ve, VelocityContext vc) {
        File f = rootDir.resolve(relPath).toFile();
        f.getParentFile().mkdirs();
        Template template = ve.getTemplate(Templates.resolveTemplateResourceName(templateId));
        try {
            try (PrintWriter out = new PrintWriter(f)) {
                template.merge(vc, out);
                out.flush();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return false;
    }

}

class DirGenTask extends GenTask {

    boolean make(Path rootDir) {
        return rootDir.resolve(relPath).toFile().mkdirs();
    }

}

class ContextChangeTask extends GenTask {

    String key;
    Object value;

    boolean change(VelocityContext vc) {
        vc.put(key, value);
        return true;
    }

}
