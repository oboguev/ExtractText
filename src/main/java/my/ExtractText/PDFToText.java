package my.ExtractText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.Loader;

public class PDFToText
{
    public static String extract(String fn) throws Exception
    {
        boolean sort = false;
        boolean separateBeads = true;
        String encoding = "UTF-8";
        String outputFile = Util.makeTempFileName();
        int startPage = 1;
        int endPage = Integer.MAX_VALUE;

        Writer output = null;
        PDDocument document = null;
        boolean ignore = false;

        try
        {
            ignore = true;
            document = Loader.loadPDF(new File(fn), "");
            ignore = false;

            AccessPermission ap = document.getCurrentAccessPermission();
            if (!ap.canExtractContent())
            {
                System.err.println("No permission to extract text from " + fn);
                return "";
            }

            output = new OutputStreamWriter(new FileOutputStream(outputFile), encoding);

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(sort);
            stripper.setShouldSeparateByBeads(separateBeads);
            stripper.setStartPage(startPage);
            stripper.setEndPage(endPage);

            // Extract text for main document:
            ignore = true;
            stripper.writeText(document, output);
            ignore = false;

            // ... also for any embedded PDFs:
            PDDocumentCatalog catalog = document.getDocumentCatalog();
            PDDocumentNameDictionary names = catalog.getNames();
            if (names != null)
            {
                PDEmbeddedFilesNameTreeNode embeddedFiles = names.getEmbeddedFiles();
                if (embeddedFiles != null)
                {
                    Map<String, PDComplexFileSpecification> embeddedFileNames = embeddedFiles.getNames();
                    if (embeddedFileNames != null)
                    {
                        for (Map.Entry<String, PDComplexFileSpecification> ent : embeddedFileNames.entrySet())
                        {
                            PDComplexFileSpecification spec = ent.getValue();
                            PDEmbeddedFile file = spec.getEmbeddedFile();
                            if (file != null && "application/pdf".equals(file.getSubtype()))
                            {
                                InputStream fis = file.createInputStream();
                                PDDocument subDoc = null;
                                try
                                {
                                    byte[] bytes = IOUtils.toByteArray(fis);
                                    subDoc = Loader.loadPDF(bytes);
                                }
                                finally
                                {
                                    fis.close();
                                }
                                try
                                {
                                    stripper.writeText(subDoc, output);
                                }
                                finally
                                {
                                    IOUtils.closeQuietly(subDoc);
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
            if (document != null)
                document.close();
            (new File(outputFile)).delete();
            if ((ex instanceof IOException) && ex.getMessage().equals("java.security.InvalidKeyException: Illegal key size"))
            {
                System.err.println("Insufficient encryption key strength to extract the text from " + fn);
                return null;
            }
            if (ignore)
            {
                System.err.println("Unable to read file " + fn);
                ex.printStackTrace();
                return null;
            }
            throw ex;
        }
        finally
        {
            IOUtils.closeQuietly(output);
            IOUtils.closeQuietly(document);
        }

        String text = Util.readFileAsUTF8(outputFile);
        (new File(outputFile)).delete();
        return text;
    }
}
