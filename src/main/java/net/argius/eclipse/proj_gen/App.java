package net.argius.eclipse.proj_gen;

import static net.argius.eclipse.proj_gen.ProjectType.*;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Semaphore;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class App extends Application implements Initializable {

    @FXML
    TextField projectName;

    @FXML
    TextField location;

    @FXML
    RadioButton plainProject;

    @FXML
    RadioButton javaProject;

    @FXML
    RadioButton maven2Project;

    @FXML
    RadioButton java6;

    @FXML
    RadioButton java7;

    @FXML
    RadioButton java8;

    @FXML
    CheckBox createWebAppFiles;

    @FXML
    CheckBox createJavaFX8Files;

    @FXML
    CheckBox createAntFile;

    @FXML
    Button button;

    @FXML
    TextArea messageArea;

    static final Logger log = LoggerFactory.getLogger(App.class);
    static final ResourceBundle res = ResourceBundle.getBundle("messages");

    private Semaphore mutex;

    public App() {
        this.mutex = new Semaphore(1);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Utils.setTextIfBlank(location, Utils.toCanonicalAbsolutePathString("./"));
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle(res.getString("title"));
        stage.setScene(new Scene(new BorderPane(), 640d, 480d));
        stage.setOnCloseRequest(x -> {
            Platform.exit();
        });
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void onMenuNewWindowSelected(ActionEvent evt) {
        // TODO how to open another window
    }

    @FXML
    void onMenuCloseSelected(ActionEvent evt) {
        Platform.exit();
    }

    @FXML
    void onMenuAboutSelected(ActionEvent evt) {
        final String msg = MessageFormat.format(res.getString("about-this-app"), versionString());
        Dialogs.create().title("About This App").message(msg).showInformation();
    }

    @FXML
    void onChooseDirectoryButtonClicked(ActionEvent evt) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("choose project dir");
        Utils.validDirectory(location.getText()).map(x -> {
            dc.setInitialDirectory(x);
            return x;
        });
        File f = dc.showDialog(null);
        if (f != null) {
            location.setText(f.getAbsolutePath());
        }
    }

    @FXML
    void onCreateButtonClicked(ActionEvent evt) {
        final String projName = projectName.getText();
        final String projDir = location.getText();
        final ProjectType type = getSelectedProjectType();
        final String javaVer = getSelectedJavaVersion();
        if (StringUtils.isBlank(projName)) {
            showAlert("The project name is empty. Stop creating.");
            return;
        }
        if (StringUtils.isBlank(projDir)) {
            showAlert("The project dir is empty. Stop creating.");
            return;
        }
        final File dir = new File(projDir, projName);
        if (dir.exists()) {
            showAlert("directory [%s] already exists.", dir);
            return;
        }
        if (mutex.tryAcquire()) {
            Utils.async(() -> {
                try {
                    createProject(dir, type, javaVer);
                } catch (Throwable th) {
                    if (log.isErrorEnabled()) {
                        log.error("when running createProject", th);
                    }
                    showAlert(th.getMessage());
                } finally {
                    mutex.release();
                }
            });
        }
        else {
            showAlert("Run another job now. Do later.");
        }
    }

    private boolean createProject(File dir, ProjectType type, String javaVersion) {
        // TODO validation
        if (dir.exists() || !dir.mkdirs()) {
            showAlert("directory %s already exists.", dir);
            return false;
        }
        final String name = projectName.getText();
        outputMessage(String.format("creating project: name = %s, type = %s", name, type));
        Map<String, Object> context = new HashMap<>();
        context.put(PropKeys.dir, dir.getAbsolutePath());
        context.put(PropKeys.projectName, name);
        context.put(PropKeys.projectId, name);
        context.put(PropKeys.rootPackage, "local");
        context.put(PropKeys.javaVersion, javaVersion);
        context.put(PropKeys.createWebAppFiles, createWebAppFiles.isSelected());
        context.put(PropKeys.createJavaFX8Files, createJavaFX8Files.isSelected());
        context.put(PropKeys.createAntFile, createAntFile.isSelected());
        Projects.createProject(type, context);
        outputMessage("project [%s] has been created.", name);
        return true;
    }

    ProjectType getSelectedProjectType() {
        for (RadioButton rb : new RadioButton[] { plainProject, javaProject, maven2Project }) {
            if (rb.isSelected()) {
                return ProjectType.fromString(rb.getText());
            }
        }
        return Unknown;
    }

    String getSelectedJavaVersion() {
        for (RadioButton rb : new RadioButton[] { java6, java7, java8 }) {
            if (rb.isSelected()) {
                return rb.getText();
            }
        }
        return "1.8";
    }

    void showAlert(String fmt, Object... args) {
        String msg = String.format(fmt, args);
        Platform.runLater(() -> Dialogs.create().title("Alert").message(msg).showWarning());
    }

    void outputMessage(String fmt, Object... args) {
        final String ss = String.format("%s: %s%n", LocalDateTime.now(), String.format(fmt, args));
        Platform.runLater(() -> messageArea.appendText(ss));
    }

    static String versionString() {
        try (InputStream is = App.class.getResourceAsStream("version")) {
            byte[] bytes = new byte[32];
            is.read(bytes);
            return new String(bytes);
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("versionString", e);
            }
        }
        return "?";
    }

    public static void main(String[] args) {
        launch(App.class);
    }

}
