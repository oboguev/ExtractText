package my.ExtractToLinux.util;

import java.nio.charset.StandardCharsets;

public class Utf8Shortener
{

    /**
     * Возвращает префикс строки s так, чтобы его длина в байтах (UTF-8)
     * была <= maxbytes. Не режет суррогатные пары.
     */
    public static String shorten(String s, int maxbytes)
    {
        if (s == null)
            throw new NullPointerException("s");

        if (maxbytes <= 0 || s.isEmpty())
            return "";

        if (s.getBytes(StandardCharsets.UTF_8).length <= maxbytes)
            return s;

        int used = 0;
        int i = 0; // индекс по char (UTF-16)
        final int n = s.length();

        while (i < n)
        {
            int cp = s.codePointAt(i);
            int cpBytes = utf8Bytes(cp);

            if (used + cpBytes > maxbytes)
                break;

            used += cpBytes;
            i += Character.charCount(cp);
        }

        return s.substring(0, i);
    }

    // Сколько байт займёт один Unicode code point в UTF-8.
    private static int utf8Bytes(int cp)
    {
        // В Java строки валидны как UTF-16; но на всякий случай:
        if (cp < 0)
            return 3; // не должно случаться
        if (cp <= 0x7F)
            return 1;
        if (cp <= 0x7FF)
            return 2;
        if (cp <= 0xFFFF)
            return 3;
        return 4;
    }

    // Быстрая проверка/пример
    public static void main(String[] args)
    {
        String s = "файл_日本語.txt🙂";
        for (int max = 1; max <= 30; max += 5)
        {
            String t = shorten(s, max);
            System.out.printf("max=%d -> '%s' bytes=%d%n",
                              max, t, t.getBytes(StandardCharsets.UTF_8).length);
        }
    }
}
