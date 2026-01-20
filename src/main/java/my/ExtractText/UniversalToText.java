package my.ExtractText;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;

public class UniversalToText
{
    public static void main(String[] args)
    {
        try
        {
            // String fn = "F:\\DOCS\\UKR-BOOKS\\рабочий класс\\Трукан - Роль рабочего класса в создании органов советской власти.rtf";
            String fn = "F:\\ScanSnap\\экономизм\\Бернштейн\\Люксембург - Марксизм и ревизионизм.docx";
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
            Metadata md = new Metadata();
            ParseContext ctx = new ParseContext();
            Parser parser = new AutoDetectParser();

            BodyContentHandler handler = new BodyContentHandler(-1);
            try (InputStream in = Files.newInputStream(Path.of(fn)))
            {
                parser.parse(in, handler, md, ctx);
            }

            // System.out.println("Detected content-type: " + md.get(Metadata.CONTENT_TYPE));

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
}
