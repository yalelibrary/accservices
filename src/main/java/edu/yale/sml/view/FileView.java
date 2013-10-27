package edu.yale.sml.view;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import edu.yale.sml.model.InputFile;
import edu.yale.sml.persistence.FileDAO;
import edu.yale.sml.persistence.FileHibernateDAO;
import edu.yale.sml.persistence.GenericHibernateDAO;
import edu.yale.sml.persistence.GenericDAO;

@ManagedBean
@RequestScoped
public class FileView
{
    List<InputFile> inputFileAsList;
    private InputFile selectedFile;

    public FileView()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    @PostConstruct
    public void init()
    {
        try
        {
            inputFileAsList = new ArrayList<InputFile>();
            GenericDAO<InputFile> fileDAO = new GenericHibernateDAO<InputFile>();
            try
            {
                inputFileAsList = fileDAO.findAll(InputFile.class);
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
           }
        catch (Exception e)
        {
            e.printStackTrace(); // todo: bug
        }
    }

    public List<String> getInputFilesByName()
    {

        FileDAO fileDAO = new FileHibernateDAO();
        List<String> fileNames = new ArrayList<String>();

        try
        {
            fileNames = fileDAO.findByName();
        }
        catch (Exception e)
        {
            e.printStackTrace(); // todo: bug
        }
        
        return fileNames;
    }

    public InputFile getSelectedFile()
    {
        return selectedFile;
    }

    public void setSelectedFile(InputFile selectedFile)
    {
        this.selectedFile = selectedFile;
    }

    public List<InputFile> getInputFileAsList()
    {
        return inputFileAsList;
    }

    public void setInputFileAsList(List<InputFile> inputFileAsList)
    {
        this.inputFileAsList = inputFileAsList;
    }

    public void removeAll()
    {
        GenericDAO<InputFile> fileDAO = new GenericHibernateDAO<InputFile>();
        try
        {
            fileDAO.delete(inputFileAsList);
            inputFileAsList.clear();

        }
        catch (Throwable e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}