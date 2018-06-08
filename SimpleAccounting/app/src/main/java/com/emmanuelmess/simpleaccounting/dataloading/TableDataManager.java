package com.emmanuelmess.simpleaccounting.dataloading;

import java.math.BigDecimal;
import java.util.ArrayList;

public class TableDataManager {

    private ArrayList<RowDataHandler> data = new ArrayList<>();

    public TableDataManager() {
        data.add(new FirstRowDataHandler());
    }

    public void addRow() {
        data.add(new RowDataHandler(data.get(data.size()-1).getTotal()));
    }

    public void updateCredit(int i, BigDecimal credit) {
        data.get(i).updateCredit(credit);
        recalculate(i);
    }

    public void updateDebit(int i, BigDecimal debit) {
        data.get(i).updateDebit(debit);
        recalculate(i);
    }

    public void updateStartingTotal(BigDecimal statingTotal) {
        ((FirstRowDataHandler) data.get(0)).updateTotal(statingTotal);
    }

    private void recalculate(int i) {
        for (int j = i+1; j < data.size(); j++) {
            data.get(j).updateLast(data.get(j-1).getTotal());
        }
    }

    public BigDecimal getTotal(int i) {
        if(i == 0) throw new IllegalArgumentException("Did you mean getStartingTotal()?");
        return data.get(i).getTotal();
    }

    public BigDecimal getStartingTotal() {
        return data.get(0).getTotal();
    }

    public void clear() {
        data.clear();
        data.add(new FirstRowDataHandler());
    }

    private static class RowDataHandler {
        private BigDecimal last, credit = BigDecimal.ZERO,
                debit = BigDecimal.ZERO, total;

        private RowDataHandler(BigDecimal last) {
            this.last = last;
            this.total = last;
        }

        public void updateLast(BigDecimal last) {
            this.last = last;
            recalculate();
        }

        public void updateCredit(BigDecimal credit) {
            this.credit = credit;
            recalculate();
        }

        public void updateDebit(BigDecimal debit) {
            this.debit = debit;
            recalculate();
        }

        public BigDecimal getTotal() {
            return total;
        }

        private void recalculate() {
            total = last.add(credit.subtract(debit));
            if(total.compareTo(BigDecimal.ZERO) == 0) {
                total = total.setScale(1, BigDecimal.ROUND_UNNECESSARY);
            }
        }
    }

    private static class FirstRowDataHandler extends RowDataHandler {
        private BigDecimal total = BigDecimal.ZERO;

        private FirstRowDataHandler() {
            super(BigDecimal.ZERO);
        }

        public void updateTotal(BigDecimal total) {
            this.total = total;
        }

        @Override
        public BigDecimal getTotal() {
            return total;
        }
    }

}
