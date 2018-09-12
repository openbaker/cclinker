/**
 * 
 */
package backend.services;

/**
 * @author schiend
 *
 */
public interface StorageService {
	
	public boolean createFile(String filePath);
	public boolean appendToFile(String filePath, String toAppend);
	
}
