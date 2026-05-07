import { Box, Typography, Card, CardContent, Switch } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';

interface NotificationSetting {
  key: string;
  icon: string;
  title: string;
  description: string;
}

const settings: NotificationSetting[] = [
  { key: 'chat', icon: '💬', title: '채팅 메시지', description: '새로운 채팅 메시지가 도착하면 알림을 표시합니다' },
  { key: 'comment', icon: '📝', title: '댓글', description: '내 게시글에 새 댓글이 달리면 알림을 표시합니다' },
  { key: 'like', icon: '❤️', title: '좋아요', description: '내 게시글에 좋아요가 눌리면 알림을 표시합니다' },
  { key: 'system', icon: '📢', title: '공지사항', description: '시스템 공지나 운영 알림을 표시합니다' },
];

function getSettings(): Record<string, boolean> {
  try {
    return JSON.parse(localStorage.getItem('notificationSettings') || '{}');
  } catch { return {}; }
}

function setSetting(key: string, value: boolean) {
  const current = getSettings();
  current[key] = value;
  localStorage.setItem('notificationSettings', JSON.stringify(current));
}

export default function NotificationSettingsPage() {
  const navigate = useNavigate();
  const [values, setValues] = useState<Record<string, boolean>>({});

  useEffect(() => {
    setValues(getSettings());
  }, []);

  const handleToggle = (key: string) => {
    const newValue = values[key] === false ? true : (values[key] === undefined ? false : !values[key]);
    setSetting(key, newValue);
    setValues({ ...values, [key]: newValue });
  };

  return (
    <Box sx={{ maxWidth: 520 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <Typography
          sx={{ fontSize: 20, cursor: 'pointer', mr: 2 }}
          onClick={() => navigate('/profile')}
        >
          &larr;
        </Typography>
        <Typography variant="h2">알림 설정</Typography>
      </Box>

      <Card>
        <CardContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            각 알림 유형별로 실시간 알림 표시 여부를 설정합니다. 알림을 끄더라도 인박스에서는 확인할 수 있습니다.
          </Typography>

          {settings.map((setting) => (
            <Box key={setting.key} sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', py: 1.5, borderBottom: '1px solid #f0f0f0', '&:last-child': { borderBottom: 'none' } }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <Box sx={{ fontSize: 20, width: 36, height: 36, display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: '#f8faf6', borderRadius: 2 }}>
                  {setting.icon}
                </Box>
                <Box>
                  <Typography sx={{ fontSize: '0.9rem', fontWeight: 500 }}>{setting.title}</Typography>
                  <Typography sx={{ fontSize: '0.75rem', color: '#888' }}>{setting.description}</Typography>
                </Box>
              </Box>
              <Switch checked={values[setting.key] !== false} onChange={() => handleToggle(setting.key)} color="primary" />
            </Box>
          ))}
        </CardContent>
      </Card>
    </Box>
  );
}
