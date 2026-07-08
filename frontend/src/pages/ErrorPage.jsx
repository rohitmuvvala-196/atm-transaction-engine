import { useNavigate } from 'react-router-dom';
import { Box, Typography, Button, Avatar } from '@mui/material';
import { ErrorOutline, Home } from '@mui/icons-material';

export default function ErrorPage() {
  const navigate = useNavigate();

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #0a1929 0%, #1a3a5c 100%)',
        p: 3,
        textAlign: 'center',
      }}
    >
      <Avatar
        sx={{
          width: 100,
          height: 100,
          bgcolor: 'error.main',
          mb: 3,
          boxShadow: '0 8px 32px rgba(255, 23, 68, 0.3)',
        }}
      >
        <ErrorOutline sx={{ fontSize: 50 }} />
      </Avatar>
      <Typography variant="h3" sx={{ fontWeight: 700, mb: 2 }}>
        404
      </Typography>
      <Typography variant="h5" sx={{ mb: 1 }}>
        Page Not Found
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4, maxWidth: 400 }}>
        The page you are looking for does not exist or has been moved.
      </Typography>
      <Button
        variant="contained"
        size="large"
        startIcon={<Home />}
        onClick={() => navigate('/dashboard')}
        sx={{ py: 1.5, px: 4 }}
      >
        Go to Dashboard
      </Button>
    </Box>
  );
}