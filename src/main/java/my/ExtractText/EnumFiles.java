package my.ExtractText;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class EnumFiles
{
    public String root = null;
    public List<FileInfo> files = new Vector<FileInfo>();
    public Map<String, FileInfo> filemap = new HashMap<String, FileInfo>();

    public static EnumFiles enumFiles(String root) throws Exception
    {
        EnumFiles ef = new EnumFiles();

        while (root.endsWith(File.separator))
            root = Util.stripTail(root, File.separator);
        root = (new File(root)).getCanonicalPath();

        if (!(new File(root)).exists())
            throw new Exception("Folder " + root + " does not exist");
        if (!(new File(root)).isDirectory())
            throw new Exception("Path " + root + " is not a directory");

        ef.root = root;
        enumFolderRecursively(ef, root);

        return ef;
    }

    private static void enumFolderRecursively(EnumFiles ef, String root) throws Exception
    {
        File rootFile = new File(root);
        for (File file : rootFile.listFiles())
        {
            if (file.isDirectory())
            {
                enumFolderRecursively(ef, file.getCanonicalPath());
            }
            else if (file.getCanonicalPath().startsWith(ef.root + File.separator))
            {
                String fp = Util.stripStart(file.getCanonicalPath(), ef.root + File.separator);
                FileInfo fi = new FileInfo();
                fi.path = fp;
                fi.lastModified = file.lastModified();
                ef.filemap.put(fp, fi);
                ef.files.add(fi);
            }
            else
            {
                throw new Exception("Unexpected path");
            }
        }
    }
}
