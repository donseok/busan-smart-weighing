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
        // HEADER BAR (Dock.Top)
        // =====================================================================
        this.headerBar = new HeaderBar();

        // =====================================================================
        // STATUS FOOTER (Dock.Bottom)
        // =====================================================================
        this.statusFooter = new StatusFooter();

        // =====================================================================
        // CONTENT AREA: Left column + divider + Right column
        // =====================================================================
        this.panelContent = new Panel();
        this.panelContent.Dock = DockStyle.Fill;
        this.panelContent.BackColor = Theme.BgBase;

        this.panelLeftCol = new Panel();
        this.panelLeftCol.Dock = DockStyle.Left;
        this.panelLeftCol.Width = 420;
        this.panelLeftCol.Padding = new Padding(Theme.SpacingMd, Theme.SpacingMd, Theme.SpacingSm, Theme.SpacingMd);
        this.panelLeftCol.BackColor = Theme.BgBase;

        this.panelDivider = new Panel();
        this.panelDivider.Dock = DockStyle.Left;
        this.panelDivider.Width = 1;
        this.panelDivider.BackColor = Theme.WithAlpha(Theme.Border, 100);

        this.panelRightCol = new Panel();
        this.panelRightCol.Dock = DockStyle.Fill;
        this.panelRightCol.Padding = new Padding(Theme.SpacingMd, Theme.SpacingMd, Theme.SpacingMd, Theme.SpacingMd);
        this.panelRightCol.BackColor = Theme.BgBase;

        // =====================================================================
        // LEFT COLUMN CONTROLS
        // =====================================================================

        // -- Weight display ---------------------------------------------------
        this.weightDisplay = new WeightDisplayPanel();
        this.weightDisplay.Dock = DockStyle.Top;
        this.weightDisplay.Height = 220;

        // -- Vehicle / dispatch info card ------------------------------------
        this.cardVehicle = new CardPanel();
        this.cardVehicle.Title = "차량 / 배차 정보";
        this.cardVehicle.AccentColor = Theme.Primary;
        this.cardVehicle.Dock = DockStyle.Top;
        this.cardVehicle.Height = 190;

        this.tableVehicle = new TableLayoutPanel();
        this.tableVehicle.Dock = DockStyle.Fill;
        this.tableVehicle.ColumnCount = 2;
        this.tableVehicle.RowCount = 5;
        this.tableVehicle.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 90F));
        this.tableVehicle.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100F));
        for (int i = 0; i < 5; i++)
            this.tableVehicle.RowStyles.Add(new RowStyle(SizeType.Percent, 20F));
        this.tableVehicle.BackColor = Color.Transparent;

        this.lblPlateLabel = new Label { Text = "차량번호", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = Theme.FontSmall, ForeColor = Theme.TextMuted };
        this.lblPlateValue = new Label { Text = "-", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = Theme.FontBodyBold, ForeColor = Theme.TextPrimary };
        this.lblCompanyLabel = new Label { Text = "업체", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = Theme.FontSmall, ForeColor = Theme.TextMuted };
        this.lblCompanyValue = new Label { Text = "-", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = Theme.FontBody, ForeColor = Theme.TextPrimary };
        this.lblItemLabel = new Label { Text = "품목", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = Theme.FontSmall, ForeColor = Theme.TextMuted };
        this.lblItemValue = new Label { Text = "-", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = Theme.FontBody, ForeColor = Theme.TextPrimary };
        this.lblDispatchLabel = new Label { Text = "배차 ID", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = Theme.FontSmall, ForeColor = Theme.TextMuted };
        this.lblDispatchValue = new Label { Text = "-", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = Theme.FontBody, ForeColor = Theme.TextPrimary };
        this.lblDriverLabel = new Label { Text = "운전자", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = Theme.FontSmall, ForeColor = Theme.TextMuted };
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

        // -- Recent history card (with ModernListView) -----------------------
        this.cardHistory = new CardPanel();
        this.cardHistory.Title = "최근 계량 기록";
        this.cardHistory.Dock = DockStyle.Fill;

        this.lvHistory = new ModernListView();
        this.lvHistory.Dock = DockStyle.Fill;
        this.lvHistory.HeaderStyle = ColumnHeaderStyle.Clickable;
        this.lvHistory.Columns.Add("시간", 85);
        this.lvHistory.Columns.Add("차량번호", 110);
        this.lvHistory.Columns.Add("중량(kg)", 90);
        this.lvHistory.Columns.Add("모드", 65);
        this.lvHistory.Columns.Add("상태", 70);

        this.ctxHistory = new ContextMenuStrip();
        this.ctxHistory.Items.Add("CSV로 내보내기", null, OnExportCsvClick);
        this.ctxHistory.Items.Add("인쇄", null, OnPrintHistoryClick);
        this.lvHistory.ContextMenuStrip = this.ctxHistory;
        this.cardHistory.Controls.Add(this.lvHistory);

        // Assemble left column (Fill first, then Top from bottom to top)
        this.panelLeftCol.Controls.Add(this.cardHistory);         // Fill
        this.panelLeftCol.Controls.Add(Spacer(Theme.SpacingSm));  // Spacer
        this.panelLeftCol.Controls.Add(this.cardVehicle);         // Top
        this.panelLeftCol.Controls.Add(Spacer(Theme.SpacingSm));  // Spacer
        this.panelLeftCol.Controls.Add(this.weightDisplay);       // Top

        // =====================================================================
        // RIGHT COLUMN CONTROLS
        // =====================================================================

        // -- Mode toggle (custom control) ------------------------------------
        this.modeToggle = new ModernToggle();
        this.modeToggle.Dock = DockStyle.Top;
        this.modeToggle.Height = 44;

        // -- Process step bar (custom control) --------------------------------
        this.processStepBar = new ProcessStepBar();
        this.processStepBar.Dock = DockStyle.Top;
        this.processStepBar.Height = 64;

        // -- Manual controls card --------------------------------------------
        this.cardManual = new CardPanel();
        this.cardManual.Title = "수동 계량 컨트롤";
        this.cardManual.Dock = DockStyle.Top;
        this.cardManual.Height = 185;
        this.cardManual.Enabled = false;

        this.lblSearchPlate = new Label();
        this.lblSearchPlate.Text = "차량번호";
        this.lblSearchPlate.Location = new Point(16, 44);
        this.lblSearchPlate.AutoSize = true;
        this.lblSearchPlate.Font = Theme.FontSmall;
        this.lblSearchPlate.ForeColor = Theme.TextMuted;

        this.txtSearchPlate = new ModernTextBox();
        this.txtSearchPlate.Location = new Point(80, 40);
        this.txtSearchPlate.Size = new Size(150, Theme.InputHeight);
        this.txtSearchPlate.Font = Theme.FontBody;
        this.txtSearchPlate.Placeholder = "12가1234";

        this.lblPlateValidation = new Label();
        this.lblPlateValidation.Text = "";
        this.lblPlateValidation.Location = new Point(80, 78);
        this.lblPlateValidation.AutoSize = true;
        this.lblPlateValidation.Font = Theme.FontCaption;
        this.lblPlateValidation.ForeColor = Theme.TextSecondary;

        this.btnSearch = new ModernButton();
        this.btnSearch.Text = "검색";
        this.btnSearch.Variant = ModernButton.ButtonVariant.Secondary;
        this.btnSearch.Location = new Point(240, 40);
        this.btnSearch.Size = new Size(80, Theme.InputHeight);
        this.btnSearch.Font = Theme.FontBody;

        this.lblSelectDispatch = new Label();
        this.lblSelectDispatch.Text = "배차";
        this.lblSelectDispatch.Location = new Point(16, 98);
        this.lblSelectDispatch.AutoSize = true;
        this.lblSelectDispatch.Font = Theme.FontSmall;
        this.lblSelectDispatch.ForeColor = Theme.TextMuted;

        this.cboDispatches = new ModernComboBox();
        this.cboDispatches.Location = new Point(80, 93);
        this.cboDispatches.Size = new Size(240, Theme.InputHeight);
        this.cboDispatches.Font = Theme.FontBody;

        this.btnConfirmWeight = new ModernButton();
        this.btnConfirmWeight.Text = "중량 확인";
        this.btnConfirmWeight.Variant = ModernButton.ButtonVariant.Primary;
        this.btnConfirmWeight.Location = new Point(80, 138);
        this.btnConfirmWeight.Size = new Size(240, 38);
        this.btnConfirmWeight.Font = Theme.FontBodyBold;

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
        this.cardActions.Height = 88;

        this.btnReWeigh = new ModernButton();
        this.btnReWeigh.Text = "재계량";
        this.btnReWeigh.Variant = ModernButton.ButtonVariant.Secondary;
        this.btnReWeigh.Location = new Point(16, 40);
        this.btnReWeigh.Size = new Size(100, 36);
        this.btnReWeigh.Font = Theme.FontBody;

        this.btnReset = new ModernButton();
        this.btnReset.Text = "초기화";
        this.btnReset.Variant = ModernButton.ButtonVariant.Secondary;
        this.btnReset.Location = new Point(124, 40);
        this.btnReset.Size = new Size(90, 36);
        this.btnReset.Font = Theme.FontBody;

        this.btnBarrierOpen = new ModernButton();
        this.btnBarrierOpen.Text = "차단기 열기";
        this.btnBarrierOpen.Variant = ModernButton.ButtonVariant.Danger;
        this.btnBarrierOpen.Location = new Point(222, 40);
        this.btnBarrierOpen.Size = new Size(100, 36);
        this.btnBarrierOpen.Font = Theme.FontBody;

        this.cardActions.Controls.Add(this.btnReWeigh);
        this.cardActions.Controls.Add(this.btnReset);
        this.cardActions.Controls.Add(this.btnBarrierOpen);

        // -- Simulator card --------------------------------------------------
        this.cardSimulator = new CardPanel();
        this.cardSimulator.Title = "시뮬레이터";
        this.cardSimulator.Dock = DockStyle.Top;
        this.cardSimulator.Height = 90;

        this.chkSimulatorMode = new ModernCheckBox();
        this.chkSimulatorMode.Text = "시뮬레이터 모드";
        this.chkSimulatorMode.Location = new Point(16, 40);
        this.chkSimulatorMode.Size = new Size(160, 24);
        this.chkSimulatorMode.Font = Theme.FontBody;

        this.btnSimSensor = new ModernButton();
        this.btnSimSensor.Text = "차량 감지";
        this.btnSimSensor.Variant = ModernButton.ButtonVariant.Secondary;
        this.btnSimSensor.Location = new Point(16, 68);
        this.btnSimSensor.Size = new Size(85, 28);
        this.btnSimSensor.Font = Theme.FontSmall;
        this.btnSimSensor.Enabled = false;

        this.btnSimLpr = new ModernButton();
        this.btnSimLpr.Text = "LPR 촬영";
        this.btnSimLpr.Variant = ModernButton.ButtonVariant.Secondary;
        this.btnSimLpr.Location = new Point(108, 68);
        this.btnSimLpr.Size = new Size(85, 28);
        this.btnSimLpr.Font = Theme.FontSmall;
        this.btnSimLpr.Enabled = false;

        this.btnSimPosition = new ModernButton();
        this.btnSimPosition.Text = "정위치";
        this.btnSimPosition.Variant = ModernButton.ButtonVariant.Secondary;
        this.btnSimPosition.Location = new Point(200, 68);
        this.btnSimPosition.Size = new Size(75, 28);
        this.btnSimPosition.Font = Theme.FontSmall;
        this.btnSimPosition.Enabled = false;

        this.btnSyncNow = new ModernButton();
        this.btnSyncNow.Text = "동기화";
        this.btnSyncNow.Variant = ModernButton.ButtonVariant.Secondary;
        this.btnSyncNow.Location = new Point(282, 68);
        this.btnSyncNow.Size = new Size(75, 28);
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

        // Assemble right column (Fill first, then Top from bottom to top)
        this.panelRightCol.Controls.Add(this.terminalLog);             // Fill
        this.panelRightCol.Controls.Add(Spacer(Theme.SpacingSm));      // Spacer
        this.panelRightCol.Controls.Add(this.cardSimulator);           // Top
        this.panelRightCol.Controls.Add(Spacer(Theme.SpacingSm));      // Spacer
        this.panelRightCol.Controls.Add(this.cardActions);             // Top
        this.panelRightCol.Controls.Add(Spacer(Theme.SpacingSm));      // Spacer
        this.panelRightCol.Controls.Add(this.cardManual);              // Top
        this.panelRightCol.Controls.Add(Spacer(Theme.SpacingSm));      // Spacer
        this.panelRightCol.Controls.Add(this.processStepBar);          // Top
        this.panelRightCol.Controls.Add(Spacer(Theme.SpacingSm));      // Spacer
        this.panelRightCol.Controls.Add(this.modeToggle);              // Top

        // Assemble content area (Fill first, then Left from right to left)
        this.panelContent.Controls.Add(this.panelRightCol);            // Fill
        this.panelContent.Controls.Add(this.panelDivider);             // Left
        this.panelContent.Controls.Add(this.panelLeftCol);             // Left

        // =====================================================================
        // Form assembly (order: Fill, then Bottom, then Top)
        // =====================================================================
        this.Controls.Add(this.panelContent);   // Fill
        this.Controls.Add(this.statusFooter);   // Bottom
        this.Controls.Add(this.headerBar);      // Top

        // =====================================================================
        // Form properties
        // =====================================================================
        this.AutoScaleDimensions = new SizeF(7F, 15F);
        this.AutoScaleMode = AutoScaleMode.Font;
        this.ClientSize = new Size(1280, 900);
        this.Text = "부산 스마트 계량 시스템";
        this.MinimumSize = new Size(1100, 700);
        this.StartPosition = FormStartPosition.CenterScreen;
        this.WindowState = FormWindowState.Maximized;
        this.Font = Theme.FontBody;
        this.DoubleBuffered = true;
        this.BackColor = Theme.BgBase;
        this.ForeColor = Theme.TextPrimary;
    }

    /// <summary>Creates a transparent spacer panel for dock-based layout.</summary>
    private static Panel Spacer(int height) =>
        new() { Dock = DockStyle.Top, Height = height, BackColor = Color.Transparent };

    #endregion

    // -- Field declarations ---------------------------------------------------

    // Layout
    private HeaderBar headerBar;
    private StatusFooter statusFooter;
    private Panel panelContent;
    private Panel panelLeftCol;
    private Panel panelDivider;
    private Panel panelRightCol;

    // Weight display
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

    // History
    private CardPanel cardHistory;
    private ModernListView lvHistory;
    private ContextMenuStrip ctxHistory;

    // Mode toggle
    private ModernToggle modeToggle;

    // Process step bar
    private ProcessStepBar processStepBar;

    // Manual controls
    private CardPanel cardManual;
    private Label lblSearchPlate;
    private ModernTextBox txtSearchPlate;
    private Label lblPlateValidation;
    private ModernButton btnSearch;
    private Label lblSelectDispatch;
    private ModernComboBox cboDispatches;
    private ModernButton btnConfirmWeight;

    // Actions
    private CardPanel cardActions;
    private ModernButton btnReWeigh;
    private ModernButton btnReset;
    private ModernButton btnBarrierOpen;

    // Terminal log
    private TerminalLogPanel terminalLog;

    // Simulator controls
    private CardPanel cardSimulator;
    private ModernCheckBox chkSimulatorMode;
    private ModernButton btnSimSensor;
    private ModernButton btnSimLpr;
    private ModernButton btnSimPosition;
    private ModernButton btnSyncNow;
}
