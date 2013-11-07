package edu.yale.sml.logic;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.codec.digest.DigestUtils;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.yale.sml.model.InputFile;
import edu.yale.sml.model.Log;
import edu.yale.sml.model.Messages;
import edu.yale.sml.persistence.FileDAO;
import edu.yale.sml.persistence.FileHibernateDAO;
import edu.yale.sml.persistence.GenericDAO;
import edu.yale.sml.persistence.GenericHibernateDAO;
import edu.yale.sml.persistence.MessagesDAO;
import edu.yale.sml.persistence.MessagesHibernateDAO;

public class LogicHelper
{

    final static Logger logger = LoggerFactory.getLogger("edu.yale.sml.logic.LogicHelper");

    /**
     * @param fileUploadController
     *            PrimeFaces component
     * @return Contents of barcode file as List<String>
     * @throws IOException
     */

    public static List<String> readFile(final UploadedFile fileUploadController) throws IOException
    {

        InputStream is = null;
        List<String> toFind = new ArrayList<String>();
        int count = 0;

        try
        {
            is = fileUploadController.getInputstream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            List<String> added = new ArrayList<String>();
            while ((line = br.readLine()) != null)
            {
                if (added.contains(line)) // 39002091235557 -- replace w/ regex
                {
                }
                sb.append(line + "\n\r");
                added.add(line);
                count++;
            }
            br.close();
            Scanner s = new Scanner(sb.toString()).useDelimiter("\n");
            while (s.hasNext())
            {
                String a = s.next().trim(); // always trim a string
                toFind.add(a);
            }
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (Exception e)
                {
                    // TODO
                }
            }
            if (count > BasicShelfScanEngine.MAX_QUERY_COUNT)
            {
                return null; // throw exception
            }
        }
        return toFind;
    }

    /**
     * @param fileUploadController
     *            PrimeFaces component
     * @return Contents of barcode file as List<String>
     * @throws IOException
     */
    public static String readFileAsString(final UploadedFile fileUploadController)
            throws IOException
    {

        StringBuilder sb = new StringBuilder();
        InputStream is = null;
        List<String> toFind = new ArrayList<String>();
        int count = 0;
        try
        {
            is = fileUploadController.getInputstream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            List<String> added = new ArrayList<String>();

            while ((line = br.readLine()) != null)
            {
                if (added.contains(line)) // 39002091235557 -- replace w/ regex
                {
                }
                sb.append(line + "\n");
                added.add(line);
                count++;
            }

            br.close();
            Scanner s = new Scanner(sb.toString()).useDelimiter("\n");
            sb = new StringBuilder();
            while (s.hasNext())
            {
                String a = s.next().trim(); // always trim a string
                sb.append(a + "\n");
            }
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (Exception e)
                {
                    logger.debug("Error closing File stream.");
                }
            }

            if (count > BasicShelfScanEngine.MAX_QUERY_COUNT)
            {
                return null; // throw exception
            }
        }
        return sb.toString();
    }

    /**
     * Saves contents from PrimeFaces component to database using DAO
     * 
     * TODO currently instanties an instance of FileDAO
     * 
     * @param fileUploadController
     * @param author
     * @param date
     * @return persistentId of the file saved .. used in history
     * @throws IOException
     */
    public static Integer saveFile(final UploadedFile fileUploadController, String author,
            String date) throws IOException
    {
        InputStream is = null;
        try
        {
            is = fileUploadController.getInputstream();
            byte[] bytes = fileUploadController.getContents();
            String md5 = DigestUtils.md5Hex(bytes);
            FileDAO dao = new FileHibernateDAO();
            List<InputFile> inputFileList = dao.findInputFileByMD5(md5);
            if (inputFileList != null && inputFileList.get(0).getName() != null
                    && inputFileList.get(0).getName().equals(fileUploadController.getFileName()))
            {
                return new Integer(inputFileList.get(0).getId());
            }

            Integer fileId = 0;
            fileId = dao.save(new InputFile(fileUploadController.getFileName(), md5, author,
                    new Date(System.currentTimeMillis()), readFileAsString(fileUploadController),
                    "sample text"));

            return fileId;
        }
        catch (Throwable e)
        {
            logger.debug("Exeption finding/saving file");
            e.printStackTrace();
            logger.debug(e.getMessage());
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (Exception e)
                {
                }
            }
        }
        return null;
    }

    /**
     * Saves contents from PrimeFaces component to database using DAO
     * 
     * TODO currently instanties an instance of FileDAO
     * 
     * @param fileUploadController
     * @param author
     * @param date
     * @return persistentId of the file saved .. used in history
     * @throws IOException
     */
    public static InputFile getInputFile(final UploadedFile fileUploadController, String author,
            String date) throws IOException
    {
        InputStream is = null;

        try
        {
            is = fileUploadController.getInputstream();
            byte[] bytes = fileUploadController.getContents();
            String md5 = DigestUtils.md5Hex(bytes);
            FileDAO dao = new FileHibernateDAO();
            List<InputFile> inputFileList = dao.findInputFileByMD5(md5);
            if (inputFileList != null && inputFileList.get(0).getName() != null
                    && inputFileList.get(0).getName().equals(fileUploadController.getFileName()))
            {
                return inputFileList.get(0);
            }
            Integer fileId = 0;
            return (new InputFile(fileUploadController.getFileName(), md5, author, new Date(
                    System.currentTimeMillis()), readFileAsString(fileUploadController),
                    "sample text"));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (Exception e)
                {
                }
            }
        }

        return null;
    }

    public static String getFileBytesAsString(InputStream is) throws IOException
    {
        List<String> toFind = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        List<String> added = new ArrayList<String>();
        while ((line = br.readLine()) != null)
        {
            sb.append(line + "\n\r");
            added.add(line);
        }
        br.close();
        Scanner s = new Scanner(sb.toString()).useDelimiter("\n");
        while (s.hasNext())
        {
            String a = s.next().trim(); // always trim a string
            toFind.add(a);
        }
        return toFind.toString();
    }

    /**
     * Get CAS user from url
     * 
     * @param wsURL
     * @param contents
     * @return
     * @throws IOException
     */
    public static List<String> getCASUser(final String cas_server_url, final StringBuffer contents)
            throws IOException
    {
        OutputStreamWriter writer = null;
        BufferedReader in = null;
        List<String> response = new ArrayList<String>();
        StringBuffer response_body = new StringBuffer();
        try
        {
            URL url = new URL(cas_server_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(contents.toString());
            writer.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            response.add(new String(Integer.toString(conn.getResponseCode())));
            String decodedString = "";
            while ((decodedString = in.readLine()) != null)
            {
                if (decodedString.length() > 0)
                {
                    response_body.append(decodedString + "\n");
                    response.add(decodedString);
                }
            }
        }
        catch (java.net.UnknownHostException e)
        {
            throw new java.net.UnknownHostException();
        }
        catch (IOException e)
        {
            throw new IOException(e);
        }
        finally
        {
            try
            {
                if (writer != null)
                {
                    writer.close();
                }
                if (in != null)
                {
                    in.close();
                }
            }
            catch (IOException e)
            {
                throw new IOException(e);
            }
        }
        return response;
    }

    // Reads from InputFileDAO

    public static List<String> readFile(String selectBoxFileName)
    {
        InputStream is = null;
        List<String> toFind = new ArrayList<String>();
        int count = 0;

        try
        {
            FileDAO dao = new FileHibernateDAO();
            String sb = dao.findContentsByFileName(selectBoxFileName);
            Scanner s = new Scanner(sb.toString()).useDelimiter("\n");
            while (s.hasNext())
            {
                String a = s.next().trim();
                toFind.add(a);
            }
        }
        catch (Throwable e)
        {
            logger.error("Exception");
            e.printStackTrace();
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            if (count > BasicShelfScanEngine.MAX_QUERY_COUNT)
            {
                return null; // throw exception
            }
        }
        return toFind;
    }

    /**
     * For reading application property
     * 
     * @param property
     * @return
     */
    public static String getApplicationProperty(String property)
    {
        try
        {
            MessagesDAO dao = new MessagesHibernateDAO();
            List<Messages> messageList = dao.findAll(Messages.class);
            for (Messages m : messageList)
            {
                if (m.getNAME().equals(property))
                {
                    return m.getVALUE();
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * For reading application property as boolean (true, false)
     * 
     * @param property
     * @return
     */
    public static boolean isApplicationPropertyChecked(String property)
    {
        try
        {
            MessagesDAO dao = new MessagesHibernateDAO();
            List<Messages> messageList = dao.findAll(Messages.class);
            for (Messages m : messageList)
            {
                if (m.getNAME().equals(property))
                {
                    if (m.getVALUE() != null)
                    {
                        if (m.getVALUE().equalsIgnoreCase("true"))
                        {
                            return true;
                        }
                    }
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Log message to tdatabase
     */
    public static void logMessage(String operation, String inputFileName, String message)
    {
        GenericDAO genericDAO = new GenericHibernateDAO();
        Log log = new Log();
        // log.setNet_id(user);
        log.setOperation(operation);
        log.setTimestamp(new Date());
        log.setInput_file(inputFileName);
        log.setStacktrace(message);
        try
        {
            genericDAO.save(log);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

}
