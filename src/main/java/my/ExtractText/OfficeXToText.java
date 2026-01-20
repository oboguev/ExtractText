package my.ExtractText;

public class OfficeXToText extends UniversalToText
{
    public static void main(String[] args)
    {
        try
        {
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
}
