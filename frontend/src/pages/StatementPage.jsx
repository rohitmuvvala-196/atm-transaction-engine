import { useState, useEffect } from 'react';
import {
  Box, Card, CardContent, Typography, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, Chip,
  CircularProgress, Paper, TablePagination,
} from '@mui/material';
import { Assessment, CheckCircle, Cancel } from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';
import { atmAPI } from '../services/api';

export default function StatementPage() {
  const { user } = useAuth();
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  useEffect(() => {
    const loadStatement = async () => {
      if (!user?.accountNumber) return;
      try {
        const response = await atmAPI.getStatement(user.accountNumber);
        setTransactions(response.data.data || []);
      } catch (error) {
        console.error('Statement load error:', error);
      } finally {
        setLoading(false);
      }
    };
    loadStatement();
  }, [user?.accountNumber]);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
        <CircularProgress size={60} />
      </Box>
    );
  }

  return (
    <Box>
      <Paper sx={{ p: 3, mb: 3, background: 'linear-gradient(135deg, #7c4dff 0%, #651fff 100%)', borderRadius: 3 }}>
        <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>Mini Statement</Typography>
        <Typography variant="body2" sx={{ opacity: 0.9 }}>
          Last 20 transactions for your account
        </Typography>
      </Paper>

      <Card>
        <CardContent sx={{ p: 0 }}>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell sx={{ fontWeight: 600 }}>Date & Time</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Type</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Amount</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Balance</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Description</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {transactions
                  .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                  .map((txn) => (
                    <TableRow key={txn.transactionId} hover>
                      <TableCell>
                        <Typography variant="body2">
                          {new Date(txn.transactionDate).toLocaleDateString('en-IN')}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {new Date(txn.transactionDate).toLocaleTimeString('en-IN')}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={txn.transactionType}
                          size="small"
                          color={
                            txn.transactionType === 'DEPOSIT' ? 'success' :
                            txn.transactionType === 'WITHDRAWAL' ? 'error' :
                            txn.transactionType === 'TRANSFER' ? 'primary' : 'default'
                          }
                          variant="outlined"
                        />
                      </TableCell>
                      <TableCell>
                        <Typography
                          variant="body2"
                          sx={{
                            fontWeight: 600,
                            color: txn.transactionType === 'DEPOSIT' ? 'success.main' : 'error.main',
                          }}
                        >
                          {txn.transactionType === 'DEPOSIT' ? '+' : '-'}₹{txn.amount?.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        ₹{txn.balanceAfter?.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                      </TableCell>
                      <TableCell>
                        {txn.successful ? (
                          <Chip icon={<CheckCircle />} label="Success" size="small" color="success" />
                        ) : (
                          <Chip icon={<Cancel />} label="Failed" size="small" color="error" />
                        )}
                      </TableCell>
                      <TableCell>
                        <Typography variant="caption" color="text.secondary">
                          {txn.description || '-'}
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ))}
                {transactions.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={6} sx={{ textAlign: 'center', py: 4 }}>
                      <Typography color="text.secondary">No transactions found</Typography>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
          <TablePagination
            component="div"
            count={transactions.length}
            page={page}
            onPageChange={(e, newPage) => setPage(newPage)}
            rowsPerPage={rowsPerPage}
            onRowsPerPageChange={(e) => {
              setRowsPerPage(parseInt(e.target.value, 10));
              setPage(0);
            }}
            rowsPerPageOptions={[5, 10, 25]}
          />
        </CardContent>
      </Card>
    </Box>
  );
}