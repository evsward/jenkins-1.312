/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.bugs;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import hudson.model.Node.Mode;
import hudson.model.Slave;
import hudson.slaves.JNLPLauncher;
import hudson.slaves.RetentionStrategy;
import hudson.slaves.DumbSlave;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.jvnet.hudson.test.Email;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.recipes.PresetData;
import org.jvnet.hudson.test.recipes.PresetData.DataSet;

import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * Makes sure that the jars that web start needs are readable, even when the anonymous user doesn't have any read access. 
 *
 * @author Kohsuke Kawaguchi
 */
public class JnlpAccessWithSecuredHudsonTest extends HudsonTestCase {

    /**
     * Creates a new slave that needs to be launched via JNLP.
     */
    protected Slave createNewJnlpSlave(String name) throws Exception {
        return new DumbSlave(name,"",System.getProperty("java.io.tmpdir")+'/'+name,"2", Mode.NORMAL, "", new JNLPLauncher(), RetentionStrategy.INSTANCE);
    }

    @PresetData(DataSet.NO_ANONYMOUS_READACCESS)
    @Email("http://www.nabble.com/Launching-slave-by-JNLP-with-Active-Directory-plugin-and-matrix-security-problem-td18980323.html")
    public void test() throws Exception {
        hudson.setSlaves(Collections.singletonList(createNewJnlpSlave("test")));
        HudsonTestCase.WebClient wc = new WebClient();
        HtmlPage p = wc.login("alice").goTo("computer/test/");

        // this fresh WebClient doesn't have a login cookie and represent JNLP launcher
        HudsonTestCase.WebClient jnlpAgent = new WebClient();

        // parse the JNLP page into DOM to list up the jars.
        XmlPage jnlp = (XmlPage) wc.goTo("computer/test/slave-agent.jnlp","application/x-java-jnlp-file");
        URL baseUrl = jnlp.getWebResponse().getUrl();
        Document dom = new DOMReader().read(jnlp.getXmlDocument());
        for( Element jar : (List<Element>)dom.selectNodes("//jar") ) {
            URL url = new URL(baseUrl,jar.attributeValue("href"));
            System.out.println(url);
            
            // now make sure that these URLs are unprotected
            Page jarResource = jnlpAgent.getPage(url);
            assertTrue(jarResource.getWebResponse().getContentType().toLowerCase().startsWith("application/"));
        }
    }
}
