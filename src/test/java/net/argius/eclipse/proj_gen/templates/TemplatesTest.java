package net.argius.eclipse.proj_gen.templates;

import static net.argius.eclipse.proj_gen.templates.Templates.*;
import static org.junit.Assert.*;
import org.junit.Test;

public final class TemplatesTest {

    @Test
    public void testResolveTemplateResourceName() {
        assertEquals("net/argius/eclipse/proj_gen/templates/abc123.vm", resolveTemplateResourceName("abc123"));
    }

}
