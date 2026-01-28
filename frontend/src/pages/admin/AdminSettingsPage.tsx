import React, { useEffect, useState } from 'react';
import {
  Tabs,
  Table,
  Input,
  InputNumber,
  Switch,
  Button,
  message,
  Typography,
  Space,
  Tag,
} from 'antd';
import { SaveOutlined, ReloadOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { SystemSetting, ApiResponse } from '../../types';

const { Title, Text } = Typography;

const categoryLabels: Record<string, string> = {
  GENERAL: '일반',
  WEIGHING: '계량',
  NOTIFICATION: '알림',
  SECURITY: '보안',
};

const AdminSettingsPage: React.FC = () => {
  const [settings, setSettings] = useState<SystemSetting[]>([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [editedValues, setEditedValues] = useState<Record<number, string>>({});
  const [activeTab, setActiveTab] = useState('GENERAL');

  const fetchSettings = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('accessToken');
      const response = await fetch('/api/v1/admin/settings', {
        headers: { Authorization: `Bearer ${token}` },
      });
      const result: ApiResponse<SystemSetting[]> = await response.json();
      if (result.success) {
        setSettings(result.data);
        setEditedValues({});
      }
    } catch (error) {
      message.error('설정을 불러오는데 실패했습니다');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSettings();
  }, []);

  const handleValueChange = (settingId: number, value: string) => {
    setEditedValues((prev) => ({ ...prev, [settingId]: value }));
  };

  const handleSave = async () => {
    const changedSettings = Object.entries(editedValues)
      .filter(([id, value]) => {
        const original = settings.find((s) => s.settingId === Number(id));
        return original && original.settingValue !== value;
      })
      .map(([id, value]) => ({
        settingId: Number(id),
        settingValue: value,
      }));

    if (changedSettings.length === 0) {
      message.info('변경된 설정이 없습니다');
      return;
    }

    setSaving(true);
    try {
      const token = localStorage.getItem('accessToken');
      const response = await fetch('/api/v1/admin/settings/bulk', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ settings: changedSettings }),
      });
      const result = await response.json();
      if (result.success) {
        message.success('설정이 저장되었습니다');
        fetchSettings();
      } else {
        message.error(result.error?.message || '저장에 실패했습니다');
      }
    } catch (error) {
      message.error('저장에 실패했습니다');
    } finally {
      setSaving(false);
    }
  };

  const renderValueInput = (record: SystemSetting) => {
    const currentValue = editedValues[record.settingId] ?? record.settingValue;
    const isChanged = editedValues[record.settingId] !== undefined
      && editedValues[record.settingId] !== record.settingValue;

    if (!record.isEditable) {
      return <Text type="secondary">{record.settingValue}</Text>;
    }

    switch (record.settingType) {
      case 'BOOLEAN':
        return (
          <Switch
            checked={currentValue === 'true'}
            onChange={(checked) => handleValueChange(record.settingId, String(checked))}
            checkedChildren="ON"
            unCheckedChildren="OFF"
          />
        );
      case 'NUMBER':
        return (
          <InputNumber
            value={Number(currentValue)}
            onChange={(val) => handleValueChange(record.settingId, String(val ?? 0))}
            style={{ width: 150, borderColor: isChanged ? '#1890ff' : undefined }}
          />
        );
      default:
        return (
          <Input
            value={currentValue}
            onChange={(e) => handleValueChange(record.settingId, e.target.value)}
            style={{ width: 250, borderColor: isChanged ? '#1890ff' : undefined }}
          />
        );
    }
  };

  const columns: ColumnsType<SystemSetting> = [
    {
      title: '설정 키',
      dataIndex: 'settingKey',
      width: 250,
      render: (key: string) => <Text code>{key}</Text>,
    },
    {
      title: '설명',
      dataIndex: 'description',
      width: 200,
    },
    {
      title: '값',
      dataIndex: 'settingValue',
      width: 280,
      render: (_, record) => renderValueInput(record),
    },
    {
      title: '타입',
      dataIndex: 'settingType',
      width: 100,
      render: (type: string) => {
        const colorMap: Record<string, string> = {
          STRING: 'blue',
          NUMBER: 'green',
          BOOLEAN: 'orange',
          JSON: 'purple',
        };
        return <Tag color={colorMap[type]}>{type}</Tag>;
      },
    },
    {
      title: '수정 가능',
      dataIndex: 'isEditable',
      width: 100,
      render: (editable: boolean) =>
        editable ? <Tag color="green">가능</Tag> : <Tag color="default">불가</Tag>,
    },
  ];

  const filteredSettings = settings.filter((s) => s.category === activeTab);

  const tabItems = ['GENERAL', 'WEIGHING', 'NOTIFICATION', 'SECURITY'].map((cat) => ({
    key: cat,
    label: categoryLabels[cat],
    children: (
      <Table
        columns={columns}
        dataSource={filteredSettings}
        rowKey="settingId"
        loading={loading}
        pagination={false}
        size="small"
      />
    ),
  }));

  const hasChanges = Object.keys(editedValues).some((id) => {
    const original = settings.find((s) => s.settingId === Number(id));
    return original && original.settingValue !== editedValues[Number(id)];
  });

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>시스템 설정</Title>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={fetchSettings} loading={loading}>
            새로고침
          </Button>
          <Button
            type="primary"
            icon={<SaveOutlined />}
            onClick={handleSave}
            loading={saving}
            disabled={!hasChanges}
          >
            저장
          </Button>
        </Space>
      </div>

      <Tabs
        activeKey={activeTab}
        onChange={setActiveTab}
        items={tabItems}
      />
    </div>
  );
};

export default AdminSettingsPage;
