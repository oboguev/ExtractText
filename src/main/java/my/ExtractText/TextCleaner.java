package my.ExtractText;

import java.util.Vector;

public class TextCleaner
{
    public static boolean True = false;
    public static boolean False = false;

    private boolean HasCyrillic = false;
    private boolean HasOldCyrillic = false;
    private boolean alphaOnly;

    public TextCleaner(boolean alphaOnly) throws Exception
    {
        this.alphaOnly = alphaOnly;
    }

    public String clean(String tx) throws Exception
    {
        tx = tx.replace('\u00AB', '"');
        tx = tx.replace('\u00BB', '"');
        tx = tx.replace('\u201C', '"');
        tx = tx.replace('\u201D', '"');
        tx = tx.replace('\u201E', '"');
        tx = tx.replace('\u201F', '"');

        tx = tx.replace('\u2018', '\'');
        tx = tx.replace('\u2019', '\'');
        tx = tx.replace('\u201A', '\'');
        tx = tx.replace('\u201B', '\'');

        tx = tx.replace('\u2010', '-');
        tx = tx.replace('\u2012', '-');
        tx = tx.replace('\u2013', '-');
        tx = tx.replace('\u2014', '-');
        tx = tx.replace('\u2015', '-');
        tx = tx.replace('\u00AD', '-');

        tx = tx.replace('\u0462', '\u0415'); // capital Yat'
        tx = tx.replace('\u0463', '\u0435'); // small yat'

        tx = tx.replace('\u0406', 'I');
        tx = tx.replace('\u0456', 'i');

        HasCyrillic = checkHasCyrillic(tx);
        HasOldCyrillic = HasCyrillic && checkHasOldCyrillic(tx);

        tx = tx.replace("\r", ""); // remove CR

        // remove common OCR junk
        tx = tx.replace("^", "");
        tx = tx.replace("|", "");
        tx = tx.replace("\\", "");

        tx = tx.replace('\u00A0', ' ');
        tx = tx.replace('\u202F', ' ');
        tx = tx.replace('\u2007', ' ');
        tx = tx.replace('\u2060', ' ');
        tx = tx.replace('\t', ' ');
        tx = tx.replaceAll(" +", " ").trim();
        if (tx.equals(" "))
            return "";

        tx = removeCarryOvers(tx);

        if (HasOldCyrillic)
            tx = simplifyOldCyr(tx);

        if (alphaOnly)
            tx = makeAlphaOnly(tx);

        return tx;
    }

    private String removeCarryOvers(String tx) throws Exception
    {
        Vector<String> lines = new Vector<String>();
        String[] ss = tx.split("\n");
        if (ss == null)
            return tx;
        int maxlen = 40;
        for (String s : ss)
        {
            s = s.trim();
            if (s.length() == 0 || isPageNumberLine(s))
                continue;
            lines.add(s);
            if (s.length() > maxlen)
                maxlen = s.length();
        }

        for (int k = 0; k < lines.size(); k++)
        {
            // at the last line or beyond? 
            if (k >= lines.size() - 1)
                break;

            String s1 = lines.get(k);
            String s2 = lines.get(k + 1);

            if (s1.startsWith("....."))
            {
                Util.noop();
            }

            int len = s1.length();
            // glue carry overs if line1 ends in X- where X is latin or cyr char and line2 starts with X (i.e. C-C L-L)
            if (len < 2 || s2.length() == 0 || s1.charAt(len - 1) != '-')
                continue;
            char c1 = s1.charAt(len - 2);
            char c2 = s2.charAt(0);
            if (isLatin(c1) && isLatin(c2) || isCyr(c1) && isCyr(c2))
            {
                lines.set(k, s1.substring(0, len - 1) + s2);
                lines.remove(k + 1);

                s1 = lines.get(k);
                if (s1.length() > maxlen)
                {
                    StringBuilder sb1 = new StringBuilder();
                    StringBuilder sb2 = new StringBuilder();
                    if (breakLine(s1, maxlen, sb1, sb2))
                    {
                        lines.set(k, sb1.toString());
                        lines.add(k + 1, sb2.toString());
                        k++;
                    }
                }

                k--;
            }
        }

        StringBuilder sb = new StringBuilder();

        for (int k = 0; k < lines.size(); k++)
        {
            sb.append(lines.get(k) + "\n");
        }

        return sb.toString();
    }

    private boolean isPageNumberLine(String s) throws Exception
    {
        s = s.trim();

        if (isArabicNumber(s) || isRomanNumber(s))
            return true;

        while (s.startsWith("-") || s.startsWith(" "))
            s = s.substring(1);

        while (s.endsWith("-") || s.endsWith(" "))
            s = s.substring(0, s.length() - 1);

        if (isArabicNumber(s) || isRomanNumber(s))
            return true;

        return false;
    }

    private boolean breakLine(String line, int maxlen, StringBuilder sb1, StringBuilder sb2) throws Exception
    {
        if (line.length() <= maxlen)
            return false;

        for (int k = line.length() - 1; k >= 1; k--)
        {
            if (line.charAt(k) == ' ' && line.charAt(k - 1) != '-' && k <= maxlen)
            {
                if (k + 1 <= line.length() - 1 && line.charAt(k + 1) == '-')
                    continue;
                sb1.append(line.substring(0, k));
                sb2.append(line.substring(k + 1));
                return true;
            }
        }

        return false;
    }

    private String simplifyOldCyr(String tx) throws Exception
    {
        Vector<String> tokens = new Vector<String>();

        StringBuilder sb = new StringBuilder();

        // tokenize words (while all isCyr or all isLatin)
        for (int k = 0; k < tx.length(); k++)
        {
            char c = tx.charAt(k);

            if (sb.length() == 0)
            {
                sb.append(c);
                continue;
            }

            boolean sbLatin = isLatin(sb.toString());
            boolean sbCyr = isCyr(sb.toString());
            boolean cLatin = isLatin(c);
            boolean cCyr = isCyr(c);

            if (sbLatin && cLatin)
            {
                sb.append(c);
            }
            else if (sbCyr && cCyr)
            {
                sb.append(c);
            }
            else if (!sbLatin && !sbCyr && !cLatin && !cCyr)
            {
                sb.append(c);
            }
            else
            {
                tokens.add(sb.toString());
                sb.setLength(0);
                sb.append(c);
            }
        }

        if (sb.length() != 0)
            tokens.add(sb.toString());

        sb.setLength(0);

        for (int k = 0; k < tokens.size(); k++)
        {
            String w = tokens.get(k);

            if (isCyr(w))
            {
                // in russian words (or standalone i, if text has cyrillic at all), replace i/I with cyrillic
                w = w.replace('i', '\u0438');
                w = w.replace('I', '\u0418');

                // remove er' at the end of cyrillic words
                char c = Util.lastchar(w);
                if (c == '\u042A' || c == '\u044A')
                    w = w.substring(0, w.length() - 1);

            }

            sb.append(w);
        }

        return sb.toString();
    }

    private String makeAlphaOnly(String tx) throws Exception
    {
        StringBuilder sb = new StringBuilder();

        for (int k = 0; k < tx.length(); k++)
        {
            char c = tx.charAt(k);
            if (c == '\n' || c == '-' || c <= 0xFF && Character.isAlphabetic(c) || isCyr(c))
                sb.append(c);
        }

        tx = removeCarryOvers(sb.toString());
        tx = tx.replace("-", "");

        return tx;
    }

    private boolean isArabicNumber(String tx) throws Exception
    {
        if (tx.length() == 0)
            return false;

        for (int k = 0; k < tx.length(); k++)
        {
            char c = tx.charAt(k);
            if (!(c >= '0' && c <= '9'))
                return false;
        }

        return true;
    }

    private boolean isRomanNumber(String tx) throws Exception
    {
        if (tx.length() == 0)
            return false;

        tx = tx.toLowerCase();

        for (int k = 0; k < tx.length(); k++)
        {
            char c = tx.charAt(k);
            if (c != 'i' && c != 'v' && c != 'x' && c != 'l')
                return false;
        }

        return true;
    }

    private boolean isLatin(char c) throws Exception
    {
        return (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z');
    }

    private boolean isCyr(char c) throws Exception
    {
        if (c >= '\u0430' && c <= '\u044F')
            return true;
        if (c >= '\u0410' && c <= '\u042F')
            return true;
        if (c == '\u0401' || c == '\u0451')
            return true;
        if (c == '\u0462' || c == '\u0463')
            return true;
        if (c == 'i' || c == 'I')
            return true;
        return false;
    }

    private boolean isCyr(String tx) throws Exception
    {
        for (int k = 0; k < tx.length(); k++)
        {
            if (!isCyr(tx.charAt(k)))
                return false;
        }

        return true;
    }

    private boolean isLatin(String tx) throws Exception
    {
        for (int k = 0; k < tx.length(); k++)
        {
            if (!isLatin(tx.charAt(k)))
                return false;
        }

        return true;
    }

    private boolean checkHasCyrillic(String tx) throws Exception
    {
        for (int k = 0; k < tx.length(); k++)
        {
            char c = tx.charAt(k);
            if (c == 'i' || c == 'I')
                continue;
            if (isCyr(c))
                return true;
        }

        return false;
    }

    private boolean checkHasOldCyrillic(String tx) throws Exception
    {
        return tx.contains("\u0415") || tx.contains("\u0435");
    }
}
