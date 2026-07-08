import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Card, CardContent, TextField, Button, Typography,
  Alert, CircularProgress, InputAdornment, Paper,
} from '@mui/material';
import { Send, AccountBalance, Info } from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';
import { atmAPI } from '../services/api';
import { toast } from 'react-toastify';

export default function TransferPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [toAccount, setToAccount] = useState('');
  const [amount, setAmount] = useState('');
  const [pin, setPin] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!user?.accountNumber) return;
    setError('');
    setLoading(true);

    try {
      const response = await atmAPI.transfer(
        user.accountNumber, toAccount, pin,
        parseFloat(amount), description
      );
      toast.success(`₹${amount} transferred successfully to ${toAccount}!`);
      navigate('/dashboard');
    } catch (err) {
      setError(err.message);
      toast.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ maxWidth: 600, mx: 'auto' }}>
      <Paper sx={{ p: 3, mb: 3, background: 'linear-gradient(135deg, #2979ff 0%, #0d47a1 100%)', borderRadius: 3 }}>
        <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>Transfer Money</Typography>
        <Typography variant="body2" sx={{ opacity: 0.9 }}>
          Transfer funds to another account
        </Typography>
      </Paper>

      <Card>
        <CardContent sx={{ p: 3 }}>
          {error && <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>{error}</Alert>}

          <form onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="From Account"
              value={user?.accountNumber || ''}
              margin="normal"
              disabled
              sx={{ mb: 2 }}
            />

            <TextField
              fullWidth
              label="To Account Number"
              value={toAccount}
              onChange={(e) => setToAccount(e.target.value)}
              margin="normal"
              required
              sx={{ mb: 2 }}
            />

            <TextField
              fullWidth
              label="Amount"
              type="number"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              margin="normal"
              required
              InputProps={{
                startAdornment: <InputAdornment position="start">₹</InputAdornment>,
                inputProps: { min: 1, step: 1 },
              }}
              sx={{ mb: 2 }}
            />

            <TextField
              fullWidth
              label="Description (Optional)"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              margin="normal"
              multiline
              rows={2}
              sx={{ mb: 2 }}
            />

            <TextField
              fullWidth
              label="PIN"
              type="password"
              value={pin}
              onChange={(e) => setPin(e.target.value)}
              margin="normal"
              required
              inputProps={{ maxLength: 6 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <AccountBalance color="primary" />
                  </InputAdornment>
                ),
              }}
              sx={{ mb: 3 }}
            />

            <Alert severity="info" sx={{ mb: 3, borderRadius: 2 }} icon={<Info />}>
              <Typography variant="caption">
                Maximum transfer amount: ₹50,000 per transaction
              </Typography>
            </Alert>

            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={loading}
              startIcon={<Send />}
              sx={{ py: 1.5 }}
            >
              {loading ? <CircularProgress size={24} color="inherit" /> : 'Transfer Money'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}