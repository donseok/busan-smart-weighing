using WeighingCS.Controls;

namespace WeighingCS;

partial class MainForm
{
    private System.ComponentModel.IContainer components = null;

    protected override void Dispose(bool disposing)
    {
        if (disposing)
        {
            components?.Dispose();
            DisposeServices();
        }
        base.Dispose(disposing);
    }

    #region Windows Form Designer generated code

    private void InitializeComponent()
    {
        this.components = new System.ComponentModel.Container();

        // =====================================================================
        // Root layout: left panel (weight + vehicle) | right panel (controls + log)
        // =====================================================================
        this.splitMain = new SplitContainer();
        this.splitMain.Dock = DockStyle.Fill;
        this.splitMain.SplitterDistance = 400;
        this.splitMain.FixedPanel = FixedPanel.None;
        this.splitMain.SplitterWidth = 2;

        // =====================================================================
        // LEFT PANEL
        // =====================================================================
        this.panelLeft = new Panel();
        this.panelLeft.Dock = DockStyle.Fill;
        this.panelLeft.Padding = new Padding(Theme.SpacingSm);

        // -- Weight display (custom control) ----------------------------------
        this.weightDisplay = new WeightDisplayPanel();
        this.weightDisplay.Dock = DockStyle.Top;
        this.weightDisplay.Height = 280;

        // -- Vehicle / dispatch info card ------------------------------------
        this.cardVehicle = new CardPanel();
        this.cardVehicle.Title = "차량 / 배차 정보";
        this.cardVehicle.AccentColor = Theme.Primary;
        this.cardVehicle.Dock = DockStyle.Top;
        this.cardVehicle.Height = 170;

        this.tableVehicle = new TableLayoutPanel();
        this.tableVehicle.Dock = DockStyle.Fill;
        this.tableVehicle.ColumnCount = 2;
        this.tableVehicle.RowCount = 5;
        this.tableVehicle.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 100F));
        this.tableVehicle.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100F));
        for (int i = 0; i < 5; i++)
            this.tableVehicle.RowStyles.Add(new RowStyle(SizeType.Percent, 20F));

        this.lblPlateLabel = new Label { Text = "차량번호:", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = Theme.FontSmallBold, ForeColor = Theme.TextSecondary };
        this.lblPlateValue = new Label { Text = "-", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = Theme.FontBody, ForeColor = Theme.TextPrimary };
        this.lblCompanyLabel = new Label { Text = "업체:", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = Theme.FontSmallBold, ForeColor = Theme.TextSecondary };
        this.lblCompanyValue = new Label { Text = "-", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = Theme.FontBody, ForeColor = Theme.TextPrimary };
        this.lblItemLabel = new Label { Text = "품목:", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = Theme.FontSmallBold, ForeColor = Theme.TextSecondary };
        this.lblItemValue = new Label { Text = "-", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = Theme.FontBody, ForeColor = Theme.TextPrimary };
        this.lblDispatchLabel = new Label { Text = "배차 ID:", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = Theme.FontSmallBold, ForeColor = Theme.TextSecondary };
        this.lblDispatchValue = new Label { Text = "-", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = Theme.FontBody, ForeColor = Theme.TextPrimary };
        this.lblDriverLabel = new Label { Text = "운전자:", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = Theme.FontSmallBold, ForeColor = Theme.TextSecondary };
        this.lblDriverValue = new Label { Text = "-", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = Theme.FontBody, ForeColor = Theme.TextPrimary };

        this.tableVehicle.Controls.Add(this.lblPlateLabel, 0, 0);
        this.tableVehicle.Controls.Add(this.lblPlateValue, 1, 0);
        this.tableVehicle.Controls.Add(this.lblCompanyLabel, 0, 1);
        this.tableVehicle.Controls.Add(this.lblCompanyValue, 1, 1);
        this.tableVehicle.Controls.Add(this.lblItemLabel, 0, 2);
        this.tableVehicle.Controls.Add(this.lblItemValue, 1, 2);
        this.tableVehicle.Controls.Add(this.lblDispatchLabel, 0, 3);
        this.tableVehicle.Controls.Add(this.lblDispatchValue, 1, 3);
        this.tableVehicle.Controls.Add(this.lblDriverLabel, 0, 4);
        this.tableVehicle.Controls.Add(this.lblDriverValue, 1, 4);

        this.cardVehicle.Controls.Add(this.tableVehicle);

        // -- Connection status bar (custom control) ---------------------------
        this.connectionBar = new ConnectionStatusPanel();
        this.connectionBar.Dock = DockStyle.Top;
        this.connectionBar.Height = 72;

        // -- Recent history card ---------------------------------------------
        this.cardHistory = new CardPanel();
        this.cardHistory.Title = "최근 계량 기록";
        this.cardHistory.Dock = DockStyle.Fill;

        this.lvHistory = new ListView();
        this.lvHistory.Dock = DockStyle.Fill;
        this.lvHistory.View = View.Details;
        this.lvHistory.FullRowSelect = true;
        this.lvHistory.GridLines = true;
        this.lvHistory.Font = Theme.FontBody;
        this.lvHistory.BorderStyle = BorderStyle.None;
        this.lvHistory.Columns.Add("시간", 80);
        this.lvHistory.Columns.Add("차량번호", 100);
        this.lvHistory.Columns.Add("중량(kg)", 90);
        this.lvHistory.Columns.Add("모드", 70);
        this.lvHistory.Columns.Add("상태", 80);
        this.lvHistory.BackColor = Theme.BgElevated;
        this.lvHistory.ForeColor = Theme.TextPrimary;

        this.ctxHistory = new ContextMenuStrip();
        this.ctxHistory.Items.Add("CSV로 내보내기", null, OnExportCsvClick);
        this.ctxHistory.Items.Add("인쇄", null, OnPrintHistoryClick);
        this.lvHistory.ContextMenuStrip = this.ctxHistory;

        this.cardHistory.Controls.Add(this.lvHistory);

        // Assemble left panel (order: Fill last, then Top from bottom-up)
        this.panelLeft.Controls.Add(this.cardHistory);       // Fill
        this.panelLeft.Controls.Add(this.connectionBar);     // Top
        this.panelLeft.Controls.Add(this.cardVehicle);       // Top
        this.panelLeft.Controls.Add(this.weightDisplay);     // Top

        this.splitMain.Panel1.Controls.Add(this.panelLeft);

        // =====================================================================
        // RIGHT PANEL
        // =====================================================================
        this.panelRight = new Panel();
        this.panelRight.Dock = DockStyle.Fill;
        this.panelRight.Padding = new Padding(Theme.SpacingSm);

        // -- Mode toggle (custom control) ------------------------------------
        this.modeToggle = new ModernToggle();
        this.modeToggle.Dock = DockStyle.Top;
        this.modeToggle.Height = 44;

        // -- Process step bar (custom control) --------------------------------
        this.processStepBar = new ProcessStepBar();
        this.processStepBar.Dock = DockStyle.Top;
        this.processStepBar.Height = 48;

        // -- Manual controls card --------------------------------------------
        this.cardManual = new CardPanel();
        this.cardManual.Title = "수동 계량 컨트롤";
        this.cardManual.Dock = DockStyle.Top;
        this.cardManual.Height = 170;
        this.cardManual.Enabled = false;

        this.lblSearchPlate = new Label();
        this.lblSearchPlate.Text = "차량번호:";
        this.lblSearchPlate.Location = new Point(16, 42);
        this.lblSearchPlate.AutoSize = true;
        this.lblSearchPlate.Font = Theme.FontBody;
        this.lblSearchPlate.ForeColor = Theme.TextSecondary;

        this.txtSearchPlate = new TextBox();
        this.txtSearchPlate.Location = new Point(90, 39);
        this.txtSearchPlate.Width = 140;
        this.txtSearchPlate.Font = Theme.FontBody;
        this.txtSearchPlate.BackColor = Theme.BgElevated;
        this.txtSearchPlate.ForeColor = Theme.TextPrimary;
        this.txtSearchPlate.BorderStyle = BorderStyle.FixedSingle;

        this.lblPlateValidation = new Label();
        this.lblPlateValidation.Text = "";
        this.lblPlateValidation.Location = new Point(90, 62);
        this.lblPlateValidation.AutoSize = true;
        this.lblPlateValidation.Font = Theme.FontSmall;
        this.lblPlateValidation.ForeColor = Theme.TextSecondary;

        this.btnSearch = new ModernButton();
        this.btnSearch.Text = "검색";
        this.btnSearch.Variant = ModernButton.ButtonVariant.Secondary;
        this.btnSearch.Location = new Point(240, 37);
        this.btnSearch.Size = new Size(80, 32);
        this.btnSearch.Font = Theme.FontBody;

        this.lblSelectDispatch = new Label();
        this.lblSelectDispatch.Text = "배차:";
        this.lblSelectDispatch.Location = new Point(16, 82);
        this.lblSelectDispatch.AutoSize = true;
        this.lblSelectDispatch.Font = Theme.FontBody;
        this.lblSelectDispatch.ForeColor = Theme.TextSecondary;

        this.cboDispatches = new ComboBox();
        this.cboDispatches.Location = new Point(90, 79);
        this.cboDispatches.Width = 230;
        this.cboDispatches.DropDownStyle = ComboBoxStyle.DropDownList;
        this.cboDispatches.Font = Theme.FontBody;
        this.cboDispatches.BackColor = Theme.BgElevated;
        this.cboDispatches.ForeColor = Theme.TextPrimary;

        this.btnConfirmWeight = new ModernButton();
        this.btnConfirmWeight.Text = "중량 확인";
        this.btnConfirmWeight.Variant = ModernButton.ButtonVariant.Primary;
        this.btnConfirmWeight.Location = new Point(90, 118);
        this.btnConfirmWeight.Size = new Size(230, 40);
        this.btnConfirmWeight.Font = Theme.FontHeading;

        this.cardManual.Controls.Add(this.lblSearchPlate);
        this.cardManual.Controls.Add(this.txtSearchPlate);
        this.cardManual.Controls.Add(this.lblPlateValidation);
        this.cardManual.Controls.Add(this.btnSearch);
        this.cardManual.Controls.Add(this.lblSelectDispatch);
        this.cardManual.Controls.Add(this.cboDispatches);
        this.cardManual.Controls.Add(this.btnConfirmWeight);

        // -- Action buttons card ---------------------------------------------
        this.cardActions = new CardPanel();
        this.cardActions.Title = "작업";
        this.cardActions.Dock = DockStyle.Top;
        this.cardActions.Height = 90;

        this.btnReWeigh = new ModernButton();
        this.btnReWeigh.Text = "재계량";
        this.btnReWeigh.Variant = ModernButton.ButtonVariant.Secondary;
        this.btnReWeigh.Location = new Point(16, 36);
        this.btnReWeigh.Size = new Size(110, 38);
        this.btnReWeigh.Font = Theme.FontBody;

        this.btnReset = new ModernButton();
        this.btnReset.Text = "초기화";
        this.btnReset.Variant = ModernButton.ButtonVariant.Secondary;
        this.btnReset.Location = new Point(136, 36);
        this.btnReset.Size = new Size(90, 38);
        this.btnReset.Font = Theme.FontBody;

        this.btnBarrierOpen = new ModernButton();
        this.btnBarrierOpen.Text = "차단기 열기";
        this.btnBarrierOpen.Variant = ModernButton.ButtonVariant.Danger;
        this.btnBarrierOpen.Location = new Point(236, 36);
        this.btnBarrierOpen.Size = new Size(110, 38);
        this.btnBarrierOpen.Font = Theme.FontBody;

        this.cardActions.Controls.Add(this.btnReWeigh);
        this.cardActions.Controls.Add(this.btnReset);
        this.cardActions.Controls.Add(this.btnBarrierOpen);

        // -- Simulator card --------------------------------------------------
        this.cardSimulator = new CardPanel();
        this.cardSimulator.Title = "시뮬레이터";
        this.cardSimulator.Dock = DockStyle.Top;
        this.cardSimulator.Height = 100;

        this.chkSimulatorMode = new CheckBox();
        this.chkSimulatorMode.Text = "시뮬레이터 모드";
        this.chkSimulatorMode.Location = new Point(16, 34);
        this.chkSimulatorMode.AutoSize = true;
        this.chkSimulatorMode.Font = Theme.FontBodyBold;
        this.chkSimulatorMode.ForeColor = Theme.TextPrimary;

        this.btnSimSensor = new ModernButton();
        this.btnSimSensor.Text = "차량 감지";
        this.btnSimSensor.Variant = ModernButton.ButtonVariant.Secondary;
        this.btnSimSensor.Location = new Point(16, 58);
        this.btnSimSensor.Size = new Size(90, 32);
        this.btnSimSensor.Font = Theme.FontSmall;
        this.btnSimSensor.Enabled = false;

        this.btnSimLpr = new ModernButton();
        this.btnSimLpr.Text = "LPR 촬영";
        this.btnSimLpr.Variant = ModernButton.ButtonVariant.Secondary;
        this.btnSimLpr.Location = new Point(114, 58);
        this.btnSimLpr.Size = new Size(90, 32);
        this.btnSimLpr.Font = Theme.FontSmall;
        this.btnSimLpr.Enabled = false;

        this.btnSimPosition = new ModernButton();
        this.btnSimPosition.Text = "정위치 토글";
        this.btnSimPosition.Variant = ModernButton.ButtonVariant.Secondary;
        this.btnSimPosition.Location = new Point(212, 58);
        this.btnSimPosition.Size = new Size(90, 32);
        this.btnSimPosition.Font = Theme.FontSmall;
        this.btnSimPosition.Enabled = false;

        this.btnSyncNow = new ModernButton();
        this.btnSyncNow.Text = "즉시 동기화";
        this.btnSyncNow.Variant = ModernButton.ButtonVariant.Secondary;
        this.btnSyncNow.Location = new Point(310, 58);
        this.btnSyncNow.Size = new Size(90, 32);
        this.btnSyncNow.Font = Theme.FontSmall;
        this.btnSyncNow.Enabled = false;

        this.cardSimulator.Controls.Add(this.chkSimulatorMode);
        this.cardSimulator.Controls.Add(this.btnSimSensor);
        this.cardSimulator.Controls.Add(this.btnSimLpr);
        this.cardSimulator.Controls.Add(this.btnSimPosition);
        this.cardSimulator.Controls.Add(this.btnSyncNow);

        // -- Terminal log (custom control) ------------------------------------
        this.terminalLog = new TerminalLogPanel();
        this.terminalLog.Dock = DockStyle.Fill;

        // Assemble right panel (Fill last, Bottom first, then Top from bottom-up)
        this.panelRight.Controls.Add(this.terminalLog);       // Fill
        this.panelRight.Controls.Add(this.cardSimulator);      // Top
        this.panelRight.Controls.Add(this.cardActions);        // Top
        this.panelRight.Controls.Add(this.cardManual);         // Top
        this.panelRight.Controls.Add(this.processStepBar);     // Top
        this.panelRight.Controls.Add(this.modeToggle);         // Top

        this.splitMain.Panel2.Controls.Add(this.panelRight);

        // =====================================================================
        // Form
        // =====================================================================
        this.AutoScaleDimensions = new SizeF(7F, 15F);
        this.AutoScaleMode = AutoScaleMode.Font;
        this.ClientSize = new Size(1180, 870);
        this.Controls.Add(this.splitMain);
        this.Text = "부산 스마트 계량 시스템";
        this.MinimumSize = new Size(1100, 700);
        this.StartPosition = FormStartPosition.CenterScreen;
        this.WindowState = FormWindowState.Maximized;
        this.Font = Theme.FontBody;
        this.DoubleBuffered = true;

        // =====================================================================
        // Dark theme styling
        // =====================================================================
        this.BackColor = Theme.BgBase;
        this.ForeColor = Theme.TextPrimary;

        this.panelLeft.BackColor = Theme.BgBase;
        this.panelRight.BackColor = Theme.BgBase;
        this.splitMain.BackColor = Theme.Border;

        this.tableVehicle.BackColor = Theme.BgSurface;
    }

    #endregion

    // -- Field declarations ---------------------------------------------------

    private SplitContainer splitMain;
    private Panel panelLeft;
    private Panel panelRight;

    // Weight display (modern)
    private WeightDisplayPanel weightDisplay;

    // Vehicle info
    private CardPanel cardVehicle;
    private TableLayoutPanel tableVehicle;
    private Label lblPlateLabel;
    private Label lblPlateValue;
    private Label lblCompanyLabel;
    private Label lblCompanyValue;
    private Label lblItemLabel;
    private Label lblItemValue;
    private Label lblDispatchLabel;
    private Label lblDispatchValue;
    private Label lblDriverLabel;
    private Label lblDriverValue;

    // Connections (modern)
    private ConnectionStatusPanel connectionBar;

    // History
    private CardPanel cardHistory;
    private ListView lvHistory;
    private ContextMenuStrip ctxHistory;

    // Mode toggle (modern)
    private ModernToggle modeToggle;

    // Process step bar (modern)
    private ProcessStepBar processStepBar;

    // Manual controls
    private CardPanel cardManual;
    private Label lblSearchPlate;
    private TextBox txtSearchPlate;
    private Label lblPlateValidation;
    private ModernButton btnSearch;
    private Label lblSelectDispatch;
    private ComboBox cboDispatches;
    private ModernButton btnConfirmWeight;

    // Actions
    private CardPanel cardActions;
    private ModernButton btnReWeigh;
    private ModernButton btnReset;
    private ModernButton btnBarrierOpen;

    // Terminal log (modern)
    private TerminalLogPanel terminalLog;

    // Simulator controls
    private CardPanel cardSimulator;
    private CheckBox chkSimulatorMode;
    private ModernButton btnSimSensor;
    private ModernButton btnSimLpr;
    private ModernButton btnSimPosition;
    private ModernButton btnSyncNow;
}
