package awsim;

import awsim.AwsScenarioConfig;
import awsim.AwsScenarioValidator;
import awsim.AwsScenarioExecutor;
import awsim.MicroserviceAutoscalingSimulator;
import awsim.MicroserviceScenarioConfig;
import awsim.MicroserviceScenarioLoader;
import awsim.MicroserviceScenarioValidator;
import awsim.AwsRegion;
import awsim.Ec2ProfileCatalog;
import awsim.AwsSimulationReport;
import awsim.MicroserviceSimulationReport;
import org.cloudbus.cloudsim.Log;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Dependency-free Swing front end for batch and microservice scenarios.
 */
public final class AwsimGui extends JFrame {
    private final JTabbedPane tabs = new JTabbedPane();
    private final JTextArea results = new JTextArea();

    private final JTextField batchTitle = field("AWSIM batch scenario");
    private final JComboBox<String> batchRegion = regionBox();
    private final JComboBox<String> batchInstance = instanceBox();
    private final JTextField batchVms = field("4");
    private final JTextField batchCloudlets = field("16");
    private final JTextField batchLength = field("900000000");
    private final JComboBox<String> batchPriceMode = priceModeBox();
    private final JTextField batchCatalog = field("");
    private final JTextField batchOfficialCache = field("target/aws-price-cache.properties");

    private final JTextField microTitle = field("AWSIM microservice autoscaling");
    private final JComboBox<String> microRegion = regionBox();
    private final JTextField microDuration = field("3600");
    private final JTextField microInterval = field("60");
    private final JTextField microArrivals = field("20,20,80,140,140,60,20");
    private final JTextArea microServices = new JTextArea(
            "api|c7i.xlarge|1|8|1|480|0.60;\n"
                    + "worker|c7i.2xlarge|1|10|1|1000|0.65",
            4,
            48);
    private final JTextField microStartup = field("60");
    private final JTextField microScaleOutCooldown = field("120");
    private final JTextField microScaleInCooldown = field("300");
    private final JTextField microSlo = field("2.0");
    private final JComboBox<String> microPriceMode = priceModeBox();
    private final JTextField microCatalog = field("");
    private final JTextField microOfficialCache = field("target/aws-microservice-price-cache.properties");

    public AwsimGui() {
        super("AWSIM — CloudSim 4.0 Cost Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(860, 680));
        tabs.addTab("Scientific batch", buildBatchPanel());
        tabs.addTab("Microservices & autoscaling", buildMicroservicePanel());
        results.setEditable(false);
        results.setLineWrap(false);
        results.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
        tabs.addTab("Results", new JScrollPane(results));
        add(tabs, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        if (args.length == 1 && "--self-check".equals(args[0])) {
            selfCheck();
            return;
        }
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println(
                    "AWSIM GUI requires a graphical desktop. Use AwsCli for headless execution.");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // The cross-platform Swing look and feel remains usable.
            }
            new AwsimGui().setVisible(true);
        });
    }

    private JPanel buildBatchPanel() {
        JPanel form = form();
        int row = 0;
        row = add(form, row, "Scenario title", batchTitle,
                "Human-readable label written to the report.");
        row = add(form, row, "AWS Region", batchRegion,
                "Selects the AWS-oriented price dimensions.");
        row = add(form, row, "EC2 instance profile", batchInstance,
                "MIPS values remain benchmark-calibration inputs.");
        row = add(form, row, "VM count", batchVms, "Number of billable VMs.");
        row = add(form, row, "Cloudlet count", batchCloudlets,
                "Number of finite scientific tasks.");
        row = add(form, row, "Cloudlet length (MI)", batchLength,
                "Million instructions; CloudSim timestamps remain seconds.");
        row = add(form, row, "Price source", batchPriceMode,
                "OFFICIAL_AUTO refreshes the public AWS Bulk API cache when stale.");
        row = add(form, row, "Explicit cache", batchCatalog,
                "Required only for CACHE mode, including official Spot history exports.");
        row = add(form, row, "Automatic cache", batchOfficialCache,
                "Small normalized cache generated from current official offer files.");

        JPanel actions = new JPanel();
        JButton validate = new JButton("Validate");
        validate.addActionListener(e -> validateBatch());
        JButton run = new JButton("Run simulation");
        run.addActionListener(e -> runBatch());
        JButton save = new JButton("Save YAML");
        save.addActionListener(e -> saveBatch());
        actions.add(validate);
        actions.add(run);
        actions.add(save);
        addActions(form, row, actions);
        return wrap(form);
    }

    private JPanel buildMicroservicePanel() {
        JPanel form = form();
        int row = 0;
        row = add(form, row, "Scenario title", microTitle,
                "Request-driven service-chain experiment.");
        row = add(form, row, "AWS Region", microRegion, "EC2 price region.");
        row = add(form, row, "Duration (s)", microDuration, "Total simulated time.");
        row = add(form, row, "Control interval (s)", microInterval,
                "Metric and scaling-decision period.");
        row = add(form, row, "Arrival rates (req/s)", microArrivals,
                "Comma-separated trace, repeated for the duration.");

        GridBagConstraints label = constraints(0, row);
        label.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Service chain"), label);
        JScrollPane servicesScroll = new JScrollPane(microServices);
        servicesScroll.setPreferredSize(new Dimension(520, 95));
        GridBagConstraints editor = constraints(1, row);
        editor.fill = GridBagConstraints.BOTH;
        editor.weightx = 1.0;
        form.add(servicesScroll, editor);
        GridBagConstraints hint = constraints(2, row);
        hint.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("<html>name|instance|min|max|initial|MI/request|target<br>"
                + "Semicolon separates services.</html>"), hint);
        row++;

        row = add(form, row, "Startup delay (s)", microStartup,
                "Time before newly requested replicas become active.");
        row = add(form, row, "Scale-out cooldown (s)", microScaleOutCooldown,
                "Minimum interval between scale-out decisions.");
        row = add(form, row, "Scale-in cooldown (s)", microScaleInCooldown,
                "Minimum interval between scale-in decisions.");
        row = add(form, row, "Response SLO (s)", microSlo,
                "Used for the explicitly approximate breach indicator.");
        row = add(form, row, "Price source", microPriceMode,
                "OFFICIAL_AUTO retrieves current On-Demand EC2 prices.");
        row = add(form, row, "Explicit cache", microCatalog,
                "Required only for CACHE mode.");
        row = add(form, row, "Automatic cache", microOfficialCache,
                "Normalized official-price cache location.");

        JPanel actions = new JPanel();
        JButton validate = new JButton("Validate");
        validate.addActionListener(e -> validateMicroservice());
        JButton run = new JButton("Run autoscaling");
        run.addActionListener(e -> runMicroservice());
        JButton save = new JButton("Save YAML");
        save.addActionListener(e -> saveMicroservice());
        actions.add(validate);
        actions.add(run);
        actions.add(save);
        addActions(form, row, actions);
        return wrap(form);
    }

    private void validateBatch() {
        try {
            AwsScenarioConfig cfg = batchConfig();
            List<String> errors = AwsScenarioValidator.validate(
                    cfg,
                    Ec2ProfileCatalog.standard().require(cfg.getInstanceType()));
            showValidation(errors, cfg.getTitle());
        } catch (Exception e) {
            showError(e);
        }
    }

    private void validateMicroservice() {
        try {
            MicroserviceScenarioConfig cfg = microConfig();
            showValidation(MicroserviceScenarioValidator.validate(cfg), cfg.getTitle());
        } catch (Exception e) {
            showError(e);
        }
    }

    private void runBatch() {
        runAsync(() -> {
            Log.disable();
            AwsSimulationReport report = new AwsScenarioExecutor().run(batchConfig());
            return report.toPrettyString();
        });
    }

    private void runMicroservice() {
        runAsync(() -> {
            MicroserviceSimulationReport report =
                    new MicroserviceAutoscalingSimulator().run(microConfig());
            return report.toPrettyString();
        });
    }

    private void runAsync(Work work) {
        results.setText("Running…\n");
        tabs.setSelectedIndex(2);
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return work.run();
            }

            @Override
            protected void done() {
                try {
                    results.setText(get());
                } catch (Exception e) {
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    results.setText("ERROR: " + cause.getMessage() + "\n");
                }
            }
        }.execute();
    }

    private AwsScenarioConfig batchConfig() {
        AwsScenarioConfig cfg = new AwsScenarioConfig();
        cfg.setTitle(batchTitle.getText().trim());
        cfg.setRegion((String) batchRegion.getSelectedItem());
        cfg.setInstanceType((String) batchInstance.getSelectedItem());
        cfg.setVmCount(Integer.parseInt(batchVms.getText().trim()));
        cfg.setCloudletCount(Integer.parseInt(batchCloudlets.getText().trim()));
        cfg.setCloudletLength(Long.parseLong(batchLength.getText().trim()));
        cfg.setPriceCatalogMode((String) batchPriceMode.getSelectedItem());
        cfg.setPriceCatalogFile(blankToNull(batchCatalog.getText()));
        cfg.setOfficialPriceCacheFile(batchOfficialCache.getText().trim());
        return cfg;
    }

    private MicroserviceScenarioConfig microConfig() {
        MicroserviceScenarioConfig cfg = new MicroserviceScenarioConfig();
        cfg.setTitle(microTitle.getText().trim());
        cfg.setRegion((String) microRegion.getSelectedItem());
        cfg.setDurationSeconds(Integer.parseInt(microDuration.getText().trim()));
        cfg.setIntervalSeconds(Integer.parseInt(microInterval.getText().trim()));
        cfg.setArrivalRatesRps(MicroserviceScenarioLoader.parseRates(microArrivals.getText()));
        cfg.setServices(MicroserviceScenarioLoader.parseServices(
                microServices.getText().replace('\n', ' ')));
        cfg.setStartupDelaySeconds(Integer.parseInt(microStartup.getText().trim()));
        cfg.setScaleOutCooldownSeconds(Integer.parseInt(microScaleOutCooldown.getText().trim()));
        cfg.setScaleInCooldownSeconds(Integer.parseInt(microScaleInCooldown.getText().trim()));
        cfg.setSloSeconds(Double.parseDouble(microSlo.getText().trim()));
        cfg.setPriceCatalogMode((String) microPriceMode.getSelectedItem());
        cfg.setPriceCatalogFile(blankToNull(microCatalog.getText()));
        cfg.setOfficialPriceCacheFile(microOfficialCache.getText().trim());
        return cfg;
    }

    private void saveBatch() {
        try {
            AwsScenarioConfig cfg = batchConfig();
            String yaml = "title: \"" + yaml(cfg.getTitle()) + "\"\n"
                    + "region: " + cfg.getRegion() + "\n"
                    + "purchaseOption: " + cfg.getPurchaseOption() + "\n"
                    + "instanceType: " + cfg.getInstanceType() + "\n"
                    + "vmCount: " + cfg.getVmCount() + "\n"
                    + "cloudletCount: " + cfg.getCloudletCount() + "\n"
                    + "cloudletLength: " + cfg.getCloudletLength() + "\n"
                    + "priceCatalogMode: " + cfg.getPriceCatalogMode() + "\n"
                    + optional("priceCatalogFile", cfg.getPriceCatalogFile())
                    + "officialPriceCacheFile: " + cfg.getOfficialPriceCacheFile() + "\n";
            chooseAndSave(yaml, "awsim-batch.yaml");
        } catch (Exception e) {
            showError(e);
        }
    }

    private void saveMicroservice() {
        try {
            MicroserviceScenarioConfig cfg = microConfig();
            String yaml = "title: \"" + yaml(cfg.getTitle()) + "\"\n"
                    + "region: " + cfg.getRegion() + "\n"
                    + "purchaseOption: " + cfg.getPurchaseOption() + "\n"
                    + "durationSeconds: " + cfg.getDurationSeconds() + "\n"
                    + "intervalSeconds: " + cfg.getIntervalSeconds() + "\n"
                    + "arrivalRatesRps: \"" + MicroserviceScenarioLoader.encodeRates(cfg.getArrivalRatesRps()) + "\"\n"
                    + "services: \"" + MicroserviceScenarioLoader.encodeServices(cfg.getServices()) + "\"\n"
                    + "startupDelaySeconds: " + cfg.getStartupDelaySeconds() + "\n"
                    + "scaleOutCooldownSeconds: " + cfg.getScaleOutCooldownSeconds() + "\n"
                    + "scaleInCooldownSeconds: " + cfg.getScaleInCooldownSeconds() + "\n"
                    + "sloSeconds: " + cfg.getSloSeconds() + "\n"
                    + "priceCatalogMode: " + cfg.getPriceCatalogMode() + "\n"
                    + optional("priceCatalogFile", cfg.getPriceCatalogFile())
                    + "officialPriceCacheFile: " + cfg.getOfficialPriceCacheFile() + "\n";
            chooseAndSave(yaml, "awsim-microservices.yaml");
        } catch (Exception e) {
            showError(e);
        }
    }

    private void chooseAndSave(String text, String defaultName) throws Exception {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(defaultName));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path path = chooser.getSelectedFile().toPath();
            Files.writeString(path, text);
            JOptionPane.showMessageDialog(this, "Saved " + path);
        }
    }

    private void showValidation(List<String> errors, String title) {
        if (errors.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Valid scenario: " + title);
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    String.join("\n", errors),
                    "Validation errors",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showError(Exception error) {
        JOptionPane.showMessageDialog(
                this,
                error.getMessage(),
                "AWSIM error",
                JOptionPane.ERROR_MESSAGE);
    }

    private static void selfCheck() {
        AwsScenarioConfig batch = new AwsScenarioConfig();
        List<String> batchErrors = AwsScenarioValidator.validate(
                batch,
                Ec2ProfileCatalog.standard().require(batch.getInstanceType()));
        MicroserviceScenarioConfig micro = new MicroserviceScenarioConfig();
        List<String> microErrors = MicroserviceScenarioValidator.validate(micro);
        if (!batchErrors.isEmpty() || !microErrors.isEmpty()) {
            throw new IllegalStateException(
                    "GUI default model is invalid: " + batchErrors + " " + microErrors);
        }
        System.out.println("AWSIM GUI SELF-CHECK: PASS");
    }

    private static JPanel form() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return panel;
    }

    private static JPanel wrap(JPanel form) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(form), BorderLayout.CENTER);
        return panel;
    }

    private static int add(
            JPanel panel,
            int row,
            String name,
            java.awt.Component input,
            String hintText) {
        GridBagConstraints label = constraints(0, row);
        label.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(name), label);
        GridBagConstraints field = constraints(1, row);
        field.fill = GridBagConstraints.HORIZONTAL;
        field.weightx = 1.0;
        panel.add(input, field);
        GridBagConstraints hint = constraints(2, row);
        hint.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("<html><small>" + hintText + "</small></html>"), hint);
        return row + 1;
    }

    private static void addActions(JPanel panel, int row, JPanel actions) {
        GridBagConstraints c = constraints(1, row);
        c.anchor = GridBagConstraints.WEST;
        panel.add(actions, c);
        GridBagConstraints spacer = constraints(0, row + 1);
        spacer.weighty = 1.0;
        panel.add(new JLabel(""), spacer);
    }

    private static GridBagConstraints constraints(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.insets = new Insets(5, 6, 5, 6);
        return c;
    }

    private static JTextField field(String value) {
        return new JTextField(value, 26);
    }

    private static JComboBox<String> priceModeBox() {
        return new JComboBox<>(new String[]{"ILLUSTRATIVE", "OFFICIAL_AUTO", "CACHE"});
    }

    private static JComboBox<String> regionBox() {
        String[] values = new String[AwsRegion.values().length];
        for (int i = 0; i < values.length; i++) {
            values[i] = AwsRegion.values()[i].getCode();
        }
        return new JComboBox<>(values);
    }

    private static JComboBox<String> instanceBox() {
        return new JComboBox<>(
                Ec2ProfileCatalog.standard().asMap().keySet().toArray(new String[0]));
    }

    private static String blankToNull(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String yaml(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String optional(String name, String value) {
        return value == null ? "" : name + ": " + value + "\n";
    }

    private interface Work {
        String run() throws Exception;
    }
}
