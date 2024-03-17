package my.ExtractText;

import java.io.File;
import java.io.FileInputStream;
// import java.io.IOException;

// import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
// import org.apache.tika.parser.rtf.RTFParser;
import org.apache.tika.parser.microsoft.rtf.RTFParser;
// import org.apache.tika.sax.BodyContentHandler;
// import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class RTFToText
{

    public static void main(String[] args)
    {
        try
        {
            String fn = "F:\\DOCS\\UKR-BOOKS\\рабочий класс\\Трукан - Роль рабочего класса в создании органов советской власти.rtf";
            String text = extract(fn);
            System.out.println(text);
            System.out.println("** Done");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static String extract(String fn) throws Exception
    {
        try
        {
            // BodyContentHandler handler = new BodyContentHandler(-1);
            RTFBodyHandler handler = new RTFBodyHandler();
            Metadata metadata = new Metadata();
            FileInputStream inputstream = new FileInputStream(new File(fn));
            ParseContext pcontext = new ParseContext();
            RTFParser parser = new RTFParser();
            parser.parse(inputstream, handler, metadata, pcontext);
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
            System.err.println("Unable to extract text from RTF file " + fn);
            ex.printStackTrace();
            return null;
        }
    }

    public static class RTFBodyHandler implements org.xml.sax.ContentHandler
    {
        private StringBuilder sb = new StringBuilder();

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
            Util.noop();
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
            return false;
        }
    }
}
