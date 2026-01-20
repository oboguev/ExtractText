package my.ExtractToLinux.data;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Describes directory content
 */
public class DirDescriptor
{
    public DirDescriptor parent;

    /* full path relative to root directory */
    public String pathRelativeToRoot;

    /* file name (last path component) */
    public String name;

    /* maps child directories names (just name, not full path) to descriptors */
    public Map<String, DirDescriptor> childDirectories = new HashMap<>();

    /* files in this directory */
    public Map<String, FileDescriptor> containedFiles = new HashMap<>();

    /* shortened dir name (last path component) */
    public String shortname;

    /* ============================================================================================== */

    public String toString()
    {
        return pathRelativeToRoot;
    }

    public String targetPath(String rootDir)
    {
        if (parent == null)
            return rootDir + File.separator + shortname;
        else
            return parent.targetPath(rootDir) + File.separator + shortname;
    }

    /* ============================================================================================== */

    public static DirDescriptor enumerateFileTree(String rootDirPath) throws Exception
    {
        Path rootPath = Paths.get(rootDirPath).toAbsolutePath().normalize();

        if (!Files.isDirectory(rootPath))
            throw new IllegalArgumentException("Root path is not a directory: " + rootDirPath);

        // Root descriptor
        DirDescriptor rootDescriptor = new DirDescriptor();
        rootDescriptor.name = rootPath.getFileName() != null
                ? rootPath.getFileName().toString()
                : ""; // filesystem root case
        rootDescriptor.pathRelativeToRoot = "";

        enumerateDirectory(rootPath, rootPath, rootDescriptor);
        return rootDescriptor;
    }

    private static void enumerateDirectory(
            Path rootPath,
            Path currentDir,
            DirDescriptor currentDirDescriptor) throws Exception
    {

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentDir))
        {
            for (Path entry : stream)
            {

                if (Files.isDirectory(entry))
                {
                    String dirName = entry.getFileName().toString();

                    DirDescriptor childDescriptor = new DirDescriptor();
                    childDescriptor.parent = currentDirDescriptor;
                    childDescriptor.name = dirName;
                    childDescriptor.pathRelativeToRoot = rootPath.relativize(entry).toString();

                    currentDirDescriptor.childDirectories.put(dirName, childDescriptor);

                    // recurse
                    enumerateDirectory(rootPath, entry, childDescriptor);
                }
                else if (Files.isRegularFile(entry))
                {

                    BasicFileAttributes attrs = Files.readAttributes(entry, BasicFileAttributes.class);

                    FileDescriptor fileDescriptor = new FileDescriptor();
                    fileDescriptor.parent = currentDirDescriptor;
                    fileDescriptor.name = entry.getFileName().toString();
                    fileDescriptor.pathRelativeToRoot = rootPath.relativize(entry).toString();
                    fileDescriptor.size = attrs.size();
                    fileDescriptor.lastModifiedTime = attrs.lastModifiedTime().toInstant();

                    currentDirDescriptor.containedFiles.put(fileDescriptor.name, fileDescriptor);
                }
            }
        }
    }

    /* ============================================================================================== */

    /**
     * Returns a flat list of this directory and all subdirectories below it.
     */
    public List<DirDescriptor> flat()
    {
        List<DirDescriptor> result = new ArrayList<>();
        collectFlat(this, result);
        return result;
    }

    private static void collectFlat(DirDescriptor current, List<DirDescriptor> out)
    {
        if (out.contains(current))
            throw new RuntimeException("BUG!");
        out.add(current);
        for (DirDescriptor child : current.childDirectories.values())
            collectFlat(child, out);
    }
}
