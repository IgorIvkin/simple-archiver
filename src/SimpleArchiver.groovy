import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class SimpleArchiver {

    private String rootPath
    private String targetZipFile
    private boolean outputFileName = false
    private List<PathMatcher> exclusionList = []

    public SimpleArchiver() {
    }

    public SimpleArchiver(String rootPath, String targetZipFile) {
        super()
        this.rootPath = rootPath
        this.targetZipFile = targetZipFile
    }

    public SimpleArchiver setRootPath(String rootPath) {
        this.rootPath = rootPath
        this
    }

    public String getRootPath() {
        this.rootPath
    }

    public SimpleArchiver setTargetZipFile(String targetZipFile) {
        this.targetZipFile = targetZipFile
        this
    }

    public String getTargetZipFile() {
        this.targetZipFile
    }

    public SimpleArchiver setOutputFileName(boolean outputFileName) {
        this.outputFileName = outputFileName
        this
    }

    public boolean getOutputFileName() {
        this.outputFileName
    }

    /**
     * Adds a new exclusion pattern to the list of exclusion.
     * @param patternToExclude a pattern to exclude from zip archive
     * @return current archiver object
     */
    public SimpleArchiver exclude(String patternToExclude) {
        this.exclusionList.add(FileSystems.default.getPathMatcher("glob:$patternToExclude"))
        this
    }

    /**
     * Runs the process of zipping taking care of excluded items.
     */
    public void zip() {
        if(!this.isRootPathExists()) {
            throw new IllegalArgumentException("Your provided root path does not exist")
        }
        this.processFolders()
    }

    private void processFolders() {
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(this.targetZipFile))
        zipOutputStream.withStream {
            this.scrollFolders(Paths.get(this.rootPath), zipOutputStream)
            zipOutputStream.finish()
        }
    }

    private void scrollFolders(Path rootPath, ZipOutputStream zipOutputStream) {
        // remember this archiver to use it inside of closure function
        SimpleArchiver that = this
        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
            FileVisitResult visitFile(Path currentFilePath, BasicFileAttributes attrs) throws IOException
            {
                // I've decided to keep these checks, they seem to be useful anyway
                Objects.requireNonNull(currentFilePath)
                Objects.requireNonNull(attrs)

                String pathInsideZipFile = rootPath.relativize(currentFilePath).normalize().toString().replaceAll("\\\\", "/")
                if(!that.isPathExcluded(currentFilePath)) {
                    if(that.getOutputFileName()) {
                        println(String.format("Zipped: %s", currentFilePath.toString()))
                    }
                    that.zipFile(
                            currentFilePath,
                            zipOutputStream,
                            pathInsideZipFile)
                }

                return FileVisitResult.CONTINUE
            }
        })
    }

    private void zipFile(Path currentFilePath,
                         ZipOutputStream zipOutputStream,
                         String pathInsideZipFile) {
        zipOutputStream.putNextEntry(new ZipEntry(pathInsideZipFile))
        try {
            if(currentFilePath.toFile().isFile()) {
                InputStream inputStream = Files.newInputStream(currentFilePath)
                zipOutputStream << inputStream
            }
        }
        finally {
            zipOutputStream.closeEntry()
        }
    }

    /**
     * Checks whether or not the defined root path exists.
     * @return true in case if defined root path exists
     */
    private boolean isRootPathExists() {
        Path rootPath = Paths.get(this.rootPath)
        Files.exists(rootPath)
    }

    /**
     * Checks whether or not the file path is in exclusion
     * patterns list.
     * @param currentFilePath
     * @return true in case if checked path is in exclusion list
     */
    private boolean isPathExcluded(Path currentFilePath) {
        return (!this.exclusionList.isEmpty() && this.exclusionList.any {it.matches(currentFilePath)})
    }

}
