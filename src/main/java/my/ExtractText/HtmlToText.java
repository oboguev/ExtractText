package my.ExtractText;

import java.io.File;
import java.io.FileInputStream;
// import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

// import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
// import org.apache.tika.sax.BodyContentHandler;
// import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class HtmlToText
{
    public static String extract(String fn) throws Exception
    {
        try
        {
            // BodyContentHandler handler = new BodyContentHandler(-1);
            HtmlBodyHandler handler = new HtmlBodyHandler();
            Metadata metadata = new Metadata();
            FileInputStream inputstream = new FileInputStream(new File(fn));
            ParseContext pcontext = new ParseContext();
            HtmlParser htmlparser = new HtmlParser();
            htmlparser.parse(inputstream, handler, metadata, pcontext);
            String text = handler.toString();
            if (text == null)
                return null;
            text = text.replace("\r\n", "\n");
            text = text.replace('\u00A0', ' ');
            text = text.replace('\u202F', ' ');
            text = text.replace('\u2007', ' ');
            text = text.replace('\u2060', ' ');
            text = text.replace('\t', ' ');
            text = text.replaceAll(" +", " ").trim();
            StringBuilder sb = new StringBuilder();
            for (String line : text.split("\n"))
            {
                line = line.trim();
                if (line.length() == 0 || line.equals(" "))
                    continue;
                sb.append(line + "\r\n");
            }
            return sb.toString();
        }
        catch (Exception ex)
        {
            System.err.println("Unable to extract text from HTML file " + fn);
            ex.printStackTrace();
            return null;
        }
    }

    public static class HtmlBodyHandler implements org.xml.sax.ContentHandler
    {
        private StringBuilder sb = new StringBuilder();
        final private static Set<String> nonBreakableElements = listNonBreakableElements();

        public String toString()
        {
            return sb.toString();
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException
        {
            for (int k = start; length-- > 0;)
            {
                sb.append(ch[k++]);
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
        {
            if (!isNonBreakableElement(localName))
                sb.append(" ");
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException
        {
            if (!isNonBreakableElement(localName))
                sb.append(" ");
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
        {
            for (int k = start; length-- > 0;)
            {
                if (ch[k++] == '\n')
                {
                    sb.append("\n");
                    return;
                }
            }

            sb.append(" ");
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException
        {
        }

        @Override
        public void skippedEntity(String name) throws SAXException
        {
        }

        @Override
        public void startDocument() throws SAXException
        {
        }

        @Override
        public void endDocument() throws SAXException
        {
        }

        @Override
        public void setDocumentLocator(Locator locator)
        {
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException
        {
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException
        {
        }

        private static boolean isNonBreakableElement(String name)
        {
            return nonBreakableElements.contains(name.toLowerCase());
        }

        private static Set<String> listNonBreakableElements()
        {
            Set<String> xs = new HashSet<String>();
            xs.add("div");
            xs.add("span");
            xs.add("a");
            xs.add("i");
            xs.add("b");
            xs.add("u");
            xs.add("tt");
            xs.add("em");
            xs.add("s");
            xs.add("strike");
            return xs;
        }
    }
}
