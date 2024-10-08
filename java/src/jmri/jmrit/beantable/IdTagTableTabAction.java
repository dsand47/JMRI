package jmri.jmrit.beantable;

import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.event.ChangeEvent;
import javax.swing.JTabbedPane;
import jmri.*;

public class IdTagTableTabAction extends AbstractTableTabAction<IdTag> {

    public IdTagTableTabAction(String s) {
        super(s);
    }

    public IdTagTableTabAction() {
        this("Multiple Tabbed");
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    protected Manager<IdTag> getManager() {
        return InstanceManager.getDefault(IdTagManager.class);
    }

    /** {@inheritDoc} */
    @Override
    protected String getClassName() {
        return IdTagTableAction.class.getName();
    }

    /** {@inheritDoc} */
    @Override
    protected IdTagTableAction getNewTableAction(String choice) {
        return new IdTagTableAction(choice);
    }

    /** {@inheritDoc} */
    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.IdTagTable";
    }

    @Override
    protected void createModel() {
        dataTabs = new JTabbedPane();
        if (getManager() instanceof jmri.managers.AbstractProxyManager) {
            // build the list, with default at start and internal at end (if present)
            jmri.managers.AbstractProxyManager<IdTag> proxy = (jmri.managers.AbstractProxyManager<IdTag>) getManager();

            tabbedTableArray.add(new TabbedTableItem<>(Bundle.getMessage("All"), true, getManager(), getNewTableAction("All"))); // NOI18N

            List<jmri.Manager<IdTag>> managerList = proxy.getDisplayOrderManagerList();
            for (Manager<IdTag> manager : managerList) {
                String manuName = manager.getMemo().getUserName();
                if (manuName == null && (manager instanceof jmri.managers.DefaultRailComManager)) {
                    manuName = "RailCom"; // NOI18N (proper name).
                }
                TabbedTableItem<IdTag> itemModel = new TabbedTableItem<>(manuName, true, manager, getNewTableAction(manuName)); // connection name to display in Tab
                tabbedTableArray.add(itemModel);
            }
            
        } else {
            String manuName = getManager().getMemo().getUserName();
            tabbedTableArray.add(new TabbedTableItem<>(manuName, true, getManager(), getNewTableAction(manuName)));
        }
        for (int x = 0; x < tabbedTableArray.size(); x++) {
            AbstractTableAction<IdTag> table = tabbedTableArray.get(x).getAAClass();
            table.addToPanel(this);
            dataTabs.addTab(tabbedTableArray.get(x).getItemString(), null, tabbedTableArray.get(x).getPanel(), null);
        }
        dataTabs.addChangeListener((ChangeEvent evt) -> setMenuBar(f));
        init = true;
    }

}
