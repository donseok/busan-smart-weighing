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

        // =====================================================================
        // LEFT PANEL
        // =====================================================================
        this.panelLeft = new Panel();
        this.panelLeft.Dock = DockStyle.Fill;

        // -- Weight display group ---------------------------------------------
        this.grpWeight = new GroupBox();
        this.grpWeight.Text = "중량 (kg)";
        this.grpWeight.Dock = DockStyle.Top;
        this.grpWeight.Height = 300;
        this.grpWeight.Padding = new Padding(10);

        this.lblWeight = new Label();
        this.lblWeight.Text = "0.0";
        this.lblWeight.Font = new Font("Consolas", 80F, FontStyle.Bold);
        this.lblWeight.TextAlign = ContentAlignment.MiddleCenter;
        this.lblWeight.Dock = DockStyle.Fill;
        this.lblWeight.ForeColor = Color.FromArgb(6, 182, 212);

        this.lblStability = new Label();
        this.lblStability.Text = "불안정";
        this.lblStability.Font = new Font("Segoe UI", 14F, FontStyle.Bold);
        this.lblStability.TextAlign = ContentAlignment.MiddleCenter;
        this.lblStability.Dock = DockStyle.Bottom;
        this.lblStability.Height = 40;
        this.lblStability.BackColor = Color.Red;
        this.lblStability.ForeColor = Color.White;

        this.grpWeight.Controls.Add(this.lblWeight);
        this.grpWeight.Controls.Add(this.lblStability);

        // -- Vehicle / dispatch info group ------------------------------------
        this.grpVehicle = new GroupBox();
        this.grpVehicle.Text = "차량 / 배차 정보";
        this.grpVehicle.Dock = DockStyle.Top;
        this.grpVehicle.Height = 160;
        this.grpVehicle.Padding = new Padding(10);
        this.grpVehicle.Top = 190;

        this.tableVehicle = new TableLayoutPanel();
        this.tableVehicle.Dock = DockStyle.Fill;
        this.tableVehicle.ColumnCount = 2;
        this.tableVehicle.RowCount = 5;
        this.tableVehicle.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 120F));
        this.tableVehicle.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100F));
        for (int i = 0; i < 5; i++)
            this.tableVehicle.RowStyles.Add(new RowStyle(SizeType.Percent, 20F));

        this.lblPlateLabel = new Label { Text = "차량번호:", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = new Font("Segoe UI", 9.5F, FontStyle.Bold) };
        this.lblPlateValue = new Label { Text = "-", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft };
        this.lblCompanyLabel = new Label { Text = "업체:", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = new Font("Segoe UI", 9.5F, FontStyle.Bold) };
        this.lblCompanyValue = new Label { Text = "-", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft };
        this.lblItemLabel = new Label { Text = "품목:", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = new Font("Segoe UI", 9.5F, FontStyle.Bold) };
        this.lblItemValue = new Label { Text = "-", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft };
        this.lblDispatchLabel = new Label { Text = "배차 ID:", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = new Font("Segoe UI", 9.5F, FontStyle.Bold) };
        this.lblDispatchValue = new Label { Text = "-", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft };
        this.lblDriverLabel = new Label { Text = "운전자:", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, Font = new Font("Segoe UI", 9.5F, FontStyle.Bold) };
        this.lblDriverValue = new Label { Text = "-", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft };

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

        this.grpVehicle.Controls.Add(this.tableVehicle);

        // -- Connection status group ------------------------------------------
        this.grpConnections = new GroupBox();
        this.grpConnections.Text = "연결 상태";
        this.grpConnections.Dock = DockStyle.Top;
        this.grpConnections.Height = 100;
        this.grpConnections.Top = 360;

        this.tableConnections = new TableLayoutPanel();
        this.tableConnections.Dock = DockStyle.Fill;
        this.tableConnections.ColumnCount = 4;
        this.tableConnections.RowCount = 3;
        this.tableConnections.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 25F));
        this.tableConnections.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 25F));
        this.tableConnections.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 25F));
        this.tableConnections.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 25F));
        this.tableConnections.RowStyles.Add(new RowStyle(SizeType.Percent, 35F));
        this.tableConnections.RowStyles.Add(new RowStyle(SizeType.Percent, 30F));
        this.tableConnections.RowStyles.Add(new RowStyle(SizeType.Percent, 35F));

        this.lblIndicatorStatus = new Label { Text = "계량기", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleCenter, Font = new Font("Segoe UI", 8F, FontStyle.Bold) };
        this.lblDisplayStatus = new Label { Text = "전광판", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleCenter, Font = new Font("Segoe UI", 8F, FontStyle.Bold) };
        this.lblBarrierStatus = new Label { Text = "차단기", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleCenter, Font = new Font("Segoe UI", 8F, FontStyle.Bold) };
        this.lblNetworkStatus = new Label { Text = "네트워크", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleCenter, Font = new Font("Segoe UI", 8F, FontStyle.Bold) };

        this.indIndicator = new Panel { BackColor = Color.Gray, Dock = DockStyle.Fill, Margin = new Padding(15, 4, 15, 4) };
        this.indDisplay = new Panel { BackColor = Color.Gray, Dock = DockStyle.Fill, Margin = new Padding(15, 4, 15, 4) };
        this.indBarrier = new Panel { BackColor = Color.Gray, Dock = DockStyle.Fill, Margin = new Padding(15, 4, 15, 4) };
        this.indNetwork = new Panel { BackColor = Color.Gray, Dock = DockStyle.Fill, Margin = new Padding(15, 4, 15, 4) };

        this.tableConnections.Controls.Add(this.lblIndicatorStatus, 0, 0);
        this.tableConnections.Controls.Add(this.lblDisplayStatus, 1, 0);
        this.tableConnections.Controls.Add(this.lblBarrierStatus, 2, 0);
        this.tableConnections.Controls.Add(this.lblNetworkStatus, 3, 0);
        this.tableConnections.Controls.Add(this.indIndicator, 0, 1);
        this.tableConnections.Controls.Add(this.indDisplay, 1, 1);
        this.tableConnections.Controls.Add(this.indBarrier, 2, 1);
        this.tableConnections.Controls.Add(this.indNetwork, 3, 1);

        this.lblIndicatorText = new Label { Text = "끊김", Dock = DockStyle.Fill, TextAlign = ContentAlignment.TopCenter, Font = new Font("Segoe UI", 7.5F), ForeColor = Color.FromArgb(148, 163, 184) };
        this.lblDisplayText = new Label { Text = "끊김", Dock = DockStyle.Fill, TextAlign = ContentAlignment.TopCenter, Font = new Font("Segoe UI", 7.5F), ForeColor = Color.FromArgb(148, 163, 184) };
        this.lblBarrierText = new Label { Text = "끊김", Dock = DockStyle.Fill, TextAlign = ContentAlignment.TopCenter, Font = new Font("Segoe UI", 7.5F), ForeColor = Color.FromArgb(148, 163, 184) };
        this.lblNetworkText = new Label { Text = "끊김", Dock = DockStyle.Fill, TextAlign = ContentAlignment.TopCenter, Font = new Font("Segoe UI", 7.5F), ForeColor = Color.FromArgb(148, 163, 184) };

        this.tableConnections.Controls.Add(this.lblIndicatorText, 0, 2);
        this.tableConnections.Controls.Add(this.lblDisplayText, 1, 2);
        this.tableConnections.Controls.Add(this.lblBarrierText, 2, 2);
        this.tableConnections.Controls.Add(this.lblNetworkText, 3, 2);

        this.grpConnections.Controls.Add(this.tableConnections);

        // -- Recent history list ----------------------------------------------
        this.grpHistory = new GroupBox();
        this.grpHistory.Text = "최근 계량 기록";
        this.grpHistory.Dock = DockStyle.Fill;

        this.lvHistory = new ListView();
        this.lvHistory.Dock = DockStyle.Fill;
        this.lvHistory.View = View.Details;
        this.lvHistory.FullRowSelect = true;
        this.lvHistory.GridLines = true;
        this.lvHistory.Font = new Font("Segoe UI", 9F);
        this.lvHistory.Columns.Add("시간", 80);
        this.lvHistory.Columns.Add("차량번호", 100);
        this.lvHistory.Columns.Add("중량(kg)", 90);
        this.lvHistory.Columns.Add("모드", 70);
        this.lvHistory.Columns.Add("상태", 80);

        this.ctxHistory = new ContextMenuStrip();
        this.ctxHistory.Items.Add("CSV로 내보내기", null, OnExportCsvClick);
        this.ctxHistory.Items.Add("인쇄", null, OnPrintHistoryClick);
        this.lvHistory.ContextMenuStrip = this.ctxHistory;

        this.grpHistory.Controls.Add(this.lvHistory);

        // Assemble left panel (order matters for Dock.Top: add bottom-first)
        this.panelLeft.Controls.Add(this.grpHistory);
        this.panelLeft.Controls.Add(this.grpConnections);
        this.panelLeft.Controls.Add(this.grpVehicle);
        this.panelLeft.Controls.Add(this.grpWeight);

        this.splitMain.Panel1.Controls.Add(this.panelLeft);

        // =====================================================================
        // RIGHT PANEL
        // =====================================================================
        this.panelRight = new Panel();
        this.panelRight.Dock = DockStyle.Fill;

        // -- Mode toggle group ------------------------------------------------
        this.grpMode = new GroupBox();
        this.grpMode.Text = "계량 모드";
        this.grpMode.Dock = DockStyle.Top;
        this.grpMode.Height = 60;

        this.rbAuto = new RadioButton();
        this.rbAuto.Text = "자동 (LPR)";
        this.rbAuto.Location = new Point(20, 25);
        this.rbAuto.AutoSize = true;
        this.rbAuto.Checked = true;
        this.rbAuto.Font = new Font("Segoe UI", 10F, FontStyle.Bold);

        this.rbManual = new RadioButton();
        this.rbManual.Text = "수동";
        this.rbManual.Location = new Point(180, 25);
        this.rbManual.AutoSize = true;
        this.rbManual.Font = new Font("Segoe UI", 10F, FontStyle.Bold);

        this.grpMode.Controls.Add(this.rbAuto);
        this.grpMode.Controls.Add(this.rbManual);

        // -- Manual controls group --------------------------------------------
        this.grpManual = new GroupBox();
        this.grpManual.Text = "수동 계량 컨트롤";
        this.grpManual.Dock = DockStyle.Top;
        this.grpManual.Height = 160;
        this.grpManual.Enabled = false;

        this.lblSearchPlate = new Label();
        this.lblSearchPlate.Text = "차량번호:";
        this.lblSearchPlate.Location = new Point(10, 28);
        this.lblSearchPlate.AutoSize = true;

        this.txtSearchPlate = new TextBox();
        this.txtSearchPlate.Location = new Point(80, 25);
        this.txtSearchPlate.Width = 140;

        this.lblPlateValidation = new Label();
        this.lblPlateValidation.Text = "";
        this.lblPlateValidation.Location = new Point(80, 48);
        this.lblPlateValidation.AutoSize = true;
        this.lblPlateValidation.Font = new Font("Segoe UI", 7.5F);
        this.lblPlateValidation.ForeColor = Color.FromArgb(148, 163, 184);

        this.btnSearch = new Button();
        this.btnSearch.Text = "검색";
        this.btnSearch.Location = new Point(230, 24);
        this.btnSearch.Width = 80;

        this.lblSelectDispatch = new Label();
        this.lblSelectDispatch.Text = "배차:";
        this.lblSelectDispatch.Location = new Point(10, 63);
        this.lblSelectDispatch.AutoSize = true;

        this.cboDispatches = new ComboBox();
        this.cboDispatches.Location = new Point(80, 60);
        this.cboDispatches.Width = 230;
        this.cboDispatches.DropDownStyle = ComboBoxStyle.DropDownList;

        this.btnConfirmWeight = new Button();
        this.btnConfirmWeight.Text = "중량 확인";
        this.btnConfirmWeight.Location = new Point(80, 100);
        this.btnConfirmWeight.Width = 230;
        this.btnConfirmWeight.Height = 40;
        this.btnConfirmWeight.Font = new Font("Segoe UI", 11F, FontStyle.Bold);
        this.btnConfirmWeight.BackColor = Color.FromArgb(6, 182, 212);
        this.btnConfirmWeight.ForeColor = Color.White;
        this.btnConfirmWeight.FlatStyle = FlatStyle.Flat;

        this.grpManual.Controls.Add(this.lblSearchPlate);
        this.grpManual.Controls.Add(this.txtSearchPlate);
        this.grpManual.Controls.Add(this.lblPlateValidation);
        this.grpManual.Controls.Add(this.btnSearch);
        this.grpManual.Controls.Add(this.lblSelectDispatch);
        this.grpManual.Controls.Add(this.cboDispatches);
        this.grpManual.Controls.Add(this.btnConfirmWeight);

        // -- Action buttons group ---------------------------------------------
        this.grpActions = new GroupBox();
        this.grpActions.Text = "작업";
        this.grpActions.Dock = DockStyle.Top;
        this.grpActions.Height = 90;

        this.btnReWeigh = new Button();
        this.btnReWeigh.Text = "재계량";
        this.btnReWeigh.Location = new Point(10, 30);
        this.btnReWeigh.Width = 120;
        this.btnReWeigh.Height = 40;
        this.btnReWeigh.Font = new Font("Segoe UI", 10F);

        this.btnReset = new Button();
        this.btnReset.Text = "초기화";
        this.btnReset.Location = new Point(140, 30);
        this.btnReset.Width = 80;
        this.btnReset.Height = 40;
        this.btnReset.Font = new Font("Segoe UI", 10F);

        this.btnBarrierOpen = new Button();
        this.btnBarrierOpen.Text = "차단기 열기";
        this.btnBarrierOpen.Location = new Point(230, 30);
        this.btnBarrierOpen.Width = 100;
        this.btnBarrierOpen.Height = 40;
        this.btnBarrierOpen.Font = new Font("Segoe UI", 9F);

        this.grpActions.Controls.Add(this.btnReWeigh);
        this.grpActions.Controls.Add(this.btnReset);
        this.grpActions.Controls.Add(this.btnBarrierOpen);

        // -- Simulator controls group --------------------------------------------
        this.grpSimulator = new GroupBox();
        this.grpSimulator.Text = "시뮬레이터";
        this.grpSimulator.Dock = DockStyle.Top;
        this.grpSimulator.Height = 110;

        this.chkSimulatorMode = new CheckBox();
        this.chkSimulatorMode.Text = "시뮬레이터 모드";
        this.chkSimulatorMode.Location = new Point(10, 25);
        this.chkSimulatorMode.AutoSize = true;
        this.chkSimulatorMode.Font = new Font("Segoe UI", 9F, FontStyle.Bold);

        this.btnSimSensor = new Button();
        this.btnSimSensor.Text = "차량 감지";
        this.btnSimSensor.Location = new Point(10, 55);
        this.btnSimSensor.Width = 90;
        this.btnSimSensor.Height = 35;
        this.btnSimSensor.Enabled = false;

        this.btnSimLpr = new Button();
        this.btnSimLpr.Text = "LPR 촬영";
        this.btnSimLpr.Location = new Point(108, 55);
        this.btnSimLpr.Width = 90;
        this.btnSimLpr.Height = 35;
        this.btnSimLpr.Enabled = false;

        this.btnSimPosition = new Button();
        this.btnSimPosition.Text = "정위치 토글";
        this.btnSimPosition.Location = new Point(206, 55);
        this.btnSimPosition.Width = 90;
        this.btnSimPosition.Height = 35;
        this.btnSimPosition.Enabled = false;

        this.btnSyncNow = new Button();
        this.btnSyncNow.Text = "즉시 동기화";
        this.btnSyncNow.Location = new Point(304, 55);
        this.btnSyncNow.Width = 90;
        this.btnSyncNow.Height = 35;
        this.btnSyncNow.Enabled = false;

        this.grpSimulator.Controls.Add(this.chkSimulatorMode);
        this.grpSimulator.Controls.Add(this.btnSimSensor);
        this.grpSimulator.Controls.Add(this.btnSimLpr);
        this.grpSimulator.Controls.Add(this.btnSimPosition);
        this.grpSimulator.Controls.Add(this.btnSyncNow);

        // -- Process state label ----------------------------------------------
        this.lblProcessState = new Label();
        this.lblProcessState.Text = "상태: 대기";
        this.lblProcessState.Dock = DockStyle.Bottom;
        this.lblProcessState.Height = 30;
        this.lblProcessState.Font = new Font("Segoe UI", 10F, FontStyle.Bold);
        this.lblProcessState.TextAlign = ContentAlignment.MiddleLeft;
        this.lblProcessState.Padding = new Padding(5, 0, 0, 0);
        this.lblProcessState.BackColor = Color.FromArgb(30, 41, 59);
        this.lblProcessState.ForeColor = Color.FromArgb(248, 250, 252);

        // -- Status / log area ------------------------------------------------
        this.grpLog = new GroupBox();
        this.grpLog.Text = "상태 로그";
        this.grpLog.Dock = DockStyle.Bottom;
        this.grpLog.Height = 150;

        this.txtLog = new RichTextBox();
        this.txtLog.Dock = DockStyle.Fill;
        this.txtLog.ReadOnly = true;
        this.txtLog.Font = new Font("Consolas", 10F);
        this.txtLog.BackColor = Color.FromArgb(15, 23, 42);
        this.txtLog.ForeColor = Color.FromArgb(16, 185, 129);

        this.grpLog.Controls.Add(this.txtLog);

        // Assemble right panel
        // Bottom-docked items first (lowest index), then top-docked (highest index docks first)
        this.panelRight.Controls.Add(this.lblProcessState);   // Bottom - above log
        this.panelRight.Controls.Add(this.grpLog);            // Bottom - very bottom
        this.panelRight.Controls.Add(this.grpSimulator);      // Top
        this.panelRight.Controls.Add(this.grpActions);        // Top
        this.panelRight.Controls.Add(this.grpManual);         // Top
        this.panelRight.Controls.Add(this.grpMode);           // Top

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
        this.Font = new Font("Segoe UI", 9F);

        // =====================================================================
        // Dark theme styling
        // =====================================================================

        // Form background
        this.BackColor = Color.FromArgb(11, 17, 32);
        this.ForeColor = Color.FromArgb(248, 250, 252);

        // Panel backgrounds
        this.panelLeft.BackColor = Color.FromArgb(11, 17, 32);
        this.panelRight.BackColor = Color.FromArgb(11, 17, 32);
        this.splitMain.BackColor = Color.FromArgb(51, 65, 85);

        // GroupBox styling
        this.grpWeight.ForeColor = Color.FromArgb(148, 163, 184);
        this.grpVehicle.ForeColor = Color.FromArgb(148, 163, 184);
        this.grpConnections.ForeColor = Color.FromArgb(148, 163, 184);
        this.grpHistory.ForeColor = Color.FromArgb(148, 163, 184);
        this.grpMode.ForeColor = Color.FromArgb(148, 163, 184);
        this.grpManual.ForeColor = Color.FromArgb(148, 163, 184);
        this.grpActions.ForeColor = Color.FromArgb(148, 163, 184);
        this.grpSimulator.ForeColor = Color.FromArgb(148, 163, 184);
        this.grpLog.ForeColor = Color.FromArgb(148, 163, 184);

        // Vehicle info value labels - white text
        this.lblPlateValue.ForeColor = Color.FromArgb(248, 250, 252);
        this.lblCompanyValue.ForeColor = Color.FromArgb(248, 250, 252);
        this.lblItemValue.ForeColor = Color.FromArgb(248, 250, 252);
        this.lblDispatchValue.ForeColor = Color.FromArgb(248, 250, 252);
        this.lblDriverValue.ForeColor = Color.FromArgb(248, 250, 252);

        // Buttons - dark theme flat style
        this.btnSearch.BackColor = Color.FromArgb(30, 41, 59);
        this.btnSearch.ForeColor = Color.FromArgb(248, 250, 252);
        this.btnSearch.FlatStyle = FlatStyle.Flat;
        this.btnSearch.FlatAppearance.BorderColor = Color.FromArgb(51, 65, 85);

        this.btnReWeigh.BackColor = Color.FromArgb(30, 41, 59);
        this.btnReWeigh.ForeColor = Color.FromArgb(248, 250, 252);
        this.btnReWeigh.FlatStyle = FlatStyle.Flat;
        this.btnReWeigh.FlatAppearance.BorderColor = Color.FromArgb(51, 65, 85);

        this.btnReset.BackColor = Color.FromArgb(30, 41, 59);
        this.btnReset.ForeColor = Color.FromArgb(248, 250, 252);
        this.btnReset.FlatStyle = FlatStyle.Flat;
        this.btnReset.FlatAppearance.BorderColor = Color.FromArgb(51, 65, 85);

        this.btnBarrierOpen.BackColor = Color.FromArgb(30, 41, 59);
        this.btnBarrierOpen.ForeColor = Color.FromArgb(248, 250, 252);
        this.btnBarrierOpen.FlatStyle = FlatStyle.Flat;
        this.btnBarrierOpen.FlatAppearance.BorderColor = Color.FromArgb(51, 65, 85);

        this.btnSimSensor.BackColor = Color.FromArgb(30, 41, 59);
        this.btnSimSensor.ForeColor = Color.FromArgb(248, 250, 252);
        this.btnSimSensor.FlatStyle = FlatStyle.Flat;
        this.btnSimSensor.FlatAppearance.BorderColor = Color.FromArgb(51, 65, 85);

        this.btnSimLpr.BackColor = Color.FromArgb(30, 41, 59);
        this.btnSimLpr.ForeColor = Color.FromArgb(248, 250, 252);
        this.btnSimLpr.FlatStyle = FlatStyle.Flat;
        this.btnSimLpr.FlatAppearance.BorderColor = Color.FromArgb(51, 65, 85);

        this.btnSimPosition.BackColor = Color.FromArgb(30, 41, 59);
        this.btnSimPosition.ForeColor = Color.FromArgb(248, 250, 252);
        this.btnSimPosition.FlatStyle = FlatStyle.Flat;
        this.btnSimPosition.FlatAppearance.BorderColor = Color.FromArgb(51, 65, 85);

        this.btnSyncNow.BackColor = Color.FromArgb(30, 41, 59);
        this.btnSyncNow.ForeColor = Color.FromArgb(248, 250, 252);
        this.btnSyncNow.FlatStyle = FlatStyle.Flat;
        this.btnSyncNow.FlatAppearance.BorderColor = Color.FromArgb(51, 65, 85);

        // TextBox - dark theme
        this.txtSearchPlate.BackColor = Color.FromArgb(15, 23, 42);
        this.txtSearchPlate.ForeColor = Color.FromArgb(248, 250, 252);

        // ComboBox - dark theme
        this.cboDispatches.BackColor = Color.FromArgb(15, 23, 42);
        this.cboDispatches.ForeColor = Color.FromArgb(248, 250, 252);

        // RadioButtons - white text
        this.rbAuto.ForeColor = Color.FromArgb(248, 250, 252);
        this.rbManual.ForeColor = Color.FromArgb(248, 250, 252);

        // CheckBox - white text
        this.chkSimulatorMode.ForeColor = Color.FromArgb(248, 250, 252);

        // ListView - dark theme
        this.lvHistory.BackColor = Color.FromArgb(15, 23, 42);
        this.lvHistory.ForeColor = Color.FromArgb(248, 250, 252);
    }

    #endregion

    // -- Field declarations ---------------------------------------------------

    private SplitContainer splitMain;
    private Panel panelLeft;
    private Panel panelRight;

    // Weight display
    private GroupBox grpWeight;
    private Label lblWeight;
    private Label lblStability;

    // Vehicle info
    private GroupBox grpVehicle;
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

    // Connections
    private GroupBox grpConnections;
    private TableLayoutPanel tableConnections;
    private Label lblIndicatorStatus;
    private Label lblDisplayStatus;
    private Label lblBarrierStatus;
    private Label lblNetworkStatus;
    private Panel indIndicator;
    private Panel indDisplay;
    private Panel indBarrier;
    private Panel indNetwork;
    private Label lblIndicatorText;
    private Label lblDisplayText;
    private Label lblBarrierText;
    private Label lblNetworkText;

    // History
    private GroupBox grpHistory;
    private ListView lvHistory;
    private ContextMenuStrip ctxHistory;

    // Mode toggle
    private GroupBox grpMode;
    private RadioButton rbAuto;
    private RadioButton rbManual;

    // Manual controls
    private GroupBox grpManual;
    private Label lblSearchPlate;
    private TextBox txtSearchPlate;
    private Label lblPlateValidation;
    private Button btnSearch;
    private Label lblSelectDispatch;
    private ComboBox cboDispatches;
    private Button btnConfirmWeight;

    // Actions
    private GroupBox grpActions;
    private Button btnReWeigh;
    private Button btnReset;
    private Button btnBarrierOpen;

    // State + log
    private Label lblProcessState;
    private GroupBox grpLog;
    private RichTextBox txtLog;

    // Simulator controls
    private GroupBox grpSimulator;
    private CheckBox chkSimulatorMode;
    private Button btnSimSensor;
    private Button btnSimLpr;
    private Button btnSimPosition;
    private Button btnSyncNow;
}
