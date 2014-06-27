package edu.yale.sml.persistence;

import java.util.List;

import edu.yale.sml.model.InputFile;

@SuppressWarnings("rawtypes")
public interface FileDAO extends GenericDAO {

    public void doIndex() throws Throwable;

    public List<InputFile> search(String searchTerm) throws Throwable;

    List findInputFileByMD5(String md5);

    public InputFile findInputFileById(Integer fileId);

    public List<String> findByName();

    public String findContentsByFileName(String fileName);
}
