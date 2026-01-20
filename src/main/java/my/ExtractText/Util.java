package my.ExtractText;

import java.util.*;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Toolkit;
import java.io.*;

public class Util
{
    public static String EOL;
    public static Boolean True = true;
    public static Boolean False = false;

    static
    {
        if (File.separatorChar == '/')
            EOL = "\n";
        else
            EOL = "\r\n";
    }

    static public void out(String s)
    {
        System.out.println(s);
    }

    static public void err(String s)
    {
        System.out.flush();
        try
        {
            Thread.currentThread().sleep(100);
        }
        catch (Exception ex)
        {
        }
        System.err.println(s);
    }

    static public byte[] readFileAsByteArray(String path) throws Exception
    {
        return Files.readAllBytes(Paths.get(path));
    }

    static public void writeAsFile(String path, byte[] bytes) throws Exception
    {
        Files.write(Paths.get(path), bytes);
    }

    static public void writeAsUTF8File(String path, String data) throws Exception
    {
        writeAsFile(path, data.getBytes(StandardCharsets.UTF_8));
    }

    static public String readFileAsUTF8(String path) throws Exception
    {
        byte[] bytes = readFileAsByteArray(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    static public String readFileAsKOI8(String path) throws Exception
    {
        if (!Charset.isSupported("KOI8_R"))
            throw new Exception("Charset KOI8_R is not supported by this Java");
        byte[] bytes = readFileAsByteArray(path);
        return new String(bytes, Charset.forName("KOI8_R"));
    }

    static public void convertFile(String srcPath, String srcEncoding, String dstPath, String dstEncoding) throws Exception
    {
        if (!Charset.isSupported(srcEncoding))
            throw new Exception("Charset " + srcEncoding + " is not supported by this Java");
        if (!Charset.isSupported(dstEncoding))
            throw new Exception("Charset " + dstEncoding + " is not supported by this Java");
        convertFile(srcPath, Charset.forName(srcEncoding), dstPath, Charset.forName(dstEncoding));
    }

    static public void convertFile(String srcPath, Charset srcEncoding, String dstPath, String dstEncoding) throws Exception
    {
        if (!Charset.isSupported(dstEncoding))
            throw new Exception("Charset " + dstEncoding + " is not supported by this Java");
        convertFile(srcPath, srcEncoding, dstPath, Charset.forName(dstEncoding));
    }

    static public void convertFile(String srcPath, String srcEncoding, String dstPath, Charset dstEncoding) throws Exception
    {
        if (!Charset.isSupported(srcEncoding))
            throw new Exception("Charset " + srcEncoding + " is not supported by this Java");
        convertFile(srcPath, Charset.forName(srcEncoding), dstPath, dstEncoding);
    }

    static public void convertFile(String srcPath, Charset srcEncoding, String dstPath, Charset dstEncoding) throws Exception
    {
        byte[] bytes = readFileAsByteArray(srcPath);
        String s = new String(bytes, srcEncoding);
        bytes = null;
        bytes = s.getBytes(dstEncoding);
        writeAsFile(dstPath, bytes);
    }

    static public char lastchar(String s) throws Exception
    {
        if (s == null)
            return 0;
        int len = s.length();
        if (len == 0)
            return 0;
        return s.charAt(len - 1);
    }

    // in("xyz", 'y')
    public static boolean in(String s, char c) throws Exception
    {
        return s.indexOf(c) != -1;
    }

    // in("aa,bb,cc", "bb")
    public static boolean in(String pat, String s) throws Exception
    {
        if (s.length() == 0 || pat.contains(s))
        {
            for (String tok : split(pat, ","))
            {
                if (tok.equals(s))
                    return true;
            }
        }
        return false;
    }

    public static String stripTail(String s, String tail) throws Exception
    {
        if (!s.endsWith(tail))
            throw new Exception("stripTail: [" + s + "] does not end with [" + tail + "]");
        return s.substring(0, s.length() - tail.length());
    }

    public static String stripStart(String s, String start) throws Exception
    {
        if (!s.startsWith(start))
            throw new Exception("stripTail: [" + s + "] does not start with [" + start + "]");
        return s.substring(start.length());
    }

    public static boolean same(String s1, String s2) throws Exception
    {
        if (s1 == null && s2 == null)
            return true;
        if (s1 == null || s2 == null)
            return false;
        return s1.equals(s2);
    }

    public static boolean same(Map<String, String> m1, Map<String, String> m2) throws Exception
    {
        if (m1 == null && m2 == null)
            return true;

        if (m1 == null || m2 == null)
            return false;

        if (m1.keySet().size() != m2.keySet().size())
            return false;

        for (String key : m1.keySet())
        {
            if (!m2.containsKey(key))
                return false;

            String v1 = m1.get(key);
            String v2 = m2.get(key);

            if (!Util.same(v1, v2))
                return false;
        }

        return true;
    }

    public static String replace_start(String word, String pre, String post) throws Exception
    {
        if (!word.startsWith(pre))
            throw new Exception("[" + word + "] does not start with [" + pre + "]");

        String term = word.substring(pre.length());

        return post + term;
    }

    public static String replace_tail(String word, String pre, String post) throws Exception
    {
        if (!word.endsWith(pre))
            throw new Exception("[" + word + "] does not end with [" + pre + "]");

        String base = word.substring(0, word.length() - pre.length());

        return base + post;
    }

    public static String despace(String text) throws Exception
    {
        if (text == null)
            return text;
        text = text.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ');
        text = text.replaceAll("\\s+", " ").trim();
        if (text.equals(" "))
            text = "";
        return text;
    }

    public static Map<String, String> parseKeyVal(String options) throws Exception
    {
        Map<String, String> map = new HashMap<String, String>();

        if (options != null)
        {
            options = despace(options);
            String[] kvs = split(options, " ");
            for (String kv : kvs)
            {
                int k = kv.indexOf('=');
                if (k != -1)
                {
                    String key = kv.substring(0, k);
                    String value = kv.substring(k + 1);
                    map.put(key, value);
                }
                else
                {
                    map.put(kv, "");
                }
            }
        }

        return map;
    }

    public static Map<String, String> parseKeyVal(List<String> args) throws Exception
    {
        Map<String, String> map = new HashMap<String, String>();

        for (String kv : args)
        {
            int k = kv.indexOf('=');
            if (k != -1)
            {
                String key = kv.substring(0, k);
                String value = kv.substring(k + 1);
                map.put(key, value);
            }
            else
            {
                map.put(kv, "");
            }
        }

        return map;
    }

    public static <E> List<E> denull(List<E> list) throws Exception
    {
        if (list == null)
            list = new Vector<E>();
        return list;
    }

    public static <E> Set<E> dup(Set<E> src) throws Exception
    {
        return new HashSet<E>(src);
    }

    public static <K, V> Map<K, V> dup(Map<K, V> src) throws Exception
    {
        return new HashMap<K, V>(src);
    }

    public static <E> List<E> dup(List<E> src) throws Exception
    {
        return new Vector<E>(src);
    }

    public static <E> Vector<E> dup(Vector<E> src) throws Exception
    {
        return new Vector<E>(src);
    }

    public static String spaces(int n) throws Exception
    {
        String s = "";
        while (n-- > 0)
            s += " ";
        return s;
    }

    public static String invert(String s)
    {
        StringBuilder sb = new StringBuilder();
        int slen = s.length();
        for (int k = 0; k < slen; k++)
            sb.append(s.charAt(slen - 1 - k));
        return sb.toString();
    }

    public static String replaceChar(String s, int index, char c) throws Exception
    {
        if (index < 0 || index >= s.length())
            throw new Exception("Character index out of bounds (" + index + " in string " + s + ")");

        char[] arr = s.toCharArray();
        arr[index] = c;
        return String.valueOf(arr);
    }

    public static List<String> product(List<String> x1, List<String> x2) throws Exception
    {
        Vector<String> xp = new Vector<String>();
        for (String e1 : x1)
        {
            for (String e2 : x2)
                xp.add(e1 + e2);
        }
        return xp;
    }

    public static <E> List<E> uniq(List<E> list) throws Exception
    {
        List<E> result = new Vector<E>();
        boolean has_last = false;
        E last = null;
        for (E el : list)
        {
            if (has_last && el.equals(last))
                continue;
            has_last = true;
            last = el;
            result.add(el);
        }
        return result;
    }

    /**
     * Get the String residing on the clipboard.
     *
     * @return any text found on the Clipboard; if none found, return null
     */
    public static String getClipboardContents() throws Exception
    {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor))
        {
            try
            {
                String result = (String) contents.getTransferData(DataFlavor.stringFlavor);
                return result;
            }
            catch (UnsupportedFlavorException | IOException ex)
            {
                throw new Exception("Unable to read the clipboard", ex);
            }
        }
        else
        {
            return null;
        }
    }

    public static void setClipboardContents(String text) throws Exception
    {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(text), null);
    }

    @SafeVarargs
    public static <E> E[] array(E... values) throws Exception
    {
        return values;
    }

    public static String[] asArray(String s, String sep) throws Exception
    {
        return split(despace(s), sep);
    }

    public static String[] asArray(String s) throws Exception
    {
        return asArray(s, guess_sep(s));
    }

    public static String[] asArray(Collection<String> it) throws Exception
    {
        String[] ar = new String[it.size()];
        int k = 0;
        for (String s : it)
            ar[k++] = s;
        return ar;
    }

    public static List<String> asList(String[] ar) throws Exception
    {
        return new Vector<String>(Arrays.asList(ar));
    }

    public static List<String> asList(String s, String sep) throws Exception
    {
        return new Vector<String>(Arrays.asList(asArray(s, sep)));
    }

    public static List<String> asList(String s) throws Exception
    {
        return asList(s, guess_sep(s));
    }

    public static List<String> asList(Iterable<String> it) throws Exception
    {
        List<String> res = new Vector<String>();
        for (String s : it)
            res.add(s);
        return res;
    }

    public static Set<String> asSet(String[] ar) throws Exception
    {
        return new HashSet<String>(Arrays.asList(ar));
    }

    public static Set<String> asSet(String s, String sep) throws Exception
    {
        return new HashSet<String>(asList(s, sep));
    }

    public static Set<String> asSet(String s) throws Exception
    {
        return asSet(s, guess_sep(s));
    }

    public static Set<String> asSet(List<String> list) throws Exception
    {
        return new HashSet<String>(list);
    }

    private static String guess_sep(String s) throws Exception
    {
        if (s.indexOf(",") != -1)
            return ",";
        else
            return " ";
    }

    public static String debrace(String s) throws Exception
    {
        if (s.startsWith("[") && s.endsWith("]") ||
            s.startsWith("(") && s.endsWith(")") ||
            s.startsWith("{") && s.endsWith("}"))
        {
            s = s.substring(1, s.length() - 1);
        }

        return s;
    }

    public static String findLongestEnding(String word, Iterable<String> it, boolean full_ok) throws Exception
    {
        String longest = "";
        for (String key : it)
        {
            if (word.endsWith(key) && key.length() > longest.length())
            {
                if (full_ok || key.length() < word.length())
                    longest = key;
            }
        }
        return longest;
    }

    public static String findLongestStart(String word, Iterable<String> it, boolean full_ok) throws Exception
    {
        String longest = "";
        for (String key : it)
        {
            if (word.startsWith(key) && key.length() > longest.length())
            {
                if (full_ok || key.length() < word.length())
                    longest = key;
            }
        }
        return longest;
    }

    public static String[] split(String s, String regex) throws Exception
    {
        if (s.length() == 0)
            return new String[0];
        else
            return s.split(regex);
    }

    public static Set<String> lowercase(Set<String> src) throws Exception
    {
        Set<String> res = new HashSet<String>();
        for (String s : src)
            res.add(s.toLowerCase());
        return res;
    }

    public static List<String> lowercase(List<String> src) throws Exception
    {
        List<String> res = new Vector<String>();
        for (String s : src)
            res.add(s.toLowerCase());
        return res;
    }

    public static String stripComment(String s) throws Exception
    {
        int k = s.indexOf('#');
        if (k != -1)
            s = s.substring(0, k);
        return s;
    }

    /*
     * Read lines from file, drop comments and despace lines.
     * Ignore blank lines
     */
    public static List<String> read_lines(String path) throws Exception
    {
        List<String> list = new Vector<String>();
        String line;

        try (FileReader fileReader = new FileReader(path);
                BufferedReader bufferedReader = new BufferedReader(fileReader))
        {
            while (null != (line = bufferedReader.readLine()))
            {
                line = stripComment(line);
                line = despace(line);
                if (line.equals(" ") || line.length() == 0)
                    continue;
                list.add(line);
            }
        }

        return list;
    }

    public static String makeTempFileName() throws Exception
    {
        String tmpfile = null;

        for (;;)
        {
            String tmpdir = System.getProperty("java.io.tmpdir");
            if (tmpdir == null || tmpdir.length() == 0)
                throw new Exception("No temporary directory");
            if (!tmpdir.endsWith(File.separator))
                tmpdir += File.separator;
            UUID uuid = UUID.randomUUID();
            tmpfile = tmpdir + uuid.toString() + ".txt";
            if (!(new File(tmpfile)).exists())
                return tmpfile;
        }
    }

    public static void noop()
    {
        // for debugging
    }
}
