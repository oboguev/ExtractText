package my.ExtractText;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

public class DjVuToText
{
    // public static String DJVUTXT = "C:\\Program Files (x86)\\DjVuLibre\\djvutxt.exe";
    public static String DJVUTXT = "F:\\Program Files (x86)\\DjVuZone\\DjVuLibre\\djvutxt.exe";

    public static String extract(String fn) throws Exception
    {
        String tmp_djvu = Util.makeTempFileName();
        String tmp_txt = Util.makeTempFileName();

        // DJVUTXT does not like spaces in file names
        Files.copy((new File(fn)).toPath(), (new File(tmp_djvu)).toPath());

        ProcessBuilder pb = new ProcessBuilder(DJVUTXT, tmp_djvu, tmp_txt);
        Process p = pb.start();
        int status = p.waitFor();
        if (status != 0)
        {
            (new File(tmp_djvu)).delete();
            (new File(tmp_txt)).delete();
            throw new Exception("Unable to extract text layer from DJVU file " + fn);
        }

        String text = Util.readFileAsUTF8(tmp_txt);
        (new File(tmp_djvu)).delete();
        (new File(tmp_txt)).delete();
        return text;
    }
}
