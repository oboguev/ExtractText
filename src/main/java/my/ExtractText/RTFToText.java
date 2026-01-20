package my.ExtractText;

public class RTFToText extends UniversalToText
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
}
