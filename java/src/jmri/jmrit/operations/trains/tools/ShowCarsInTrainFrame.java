package jmri.jmrit.operations.trains.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.*;
import jmri.jmrit.operations.trains.trainbuilder.TrainCommon;

/**
 * Show Cars In Train Frame. This frame lists all cars assigned to a train in
 * the correct blocking order. Also show which cars are to be picked up and set
 * out at each location in the train's route.
 *
 * @author Dan Boudreau Copyright (C) 2012
 */
public class ShowCarsInTrainFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    Train _train = null;
    CarManager carManager = InstanceManager.getDefault(CarManager.class);
    TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);

    // labels
    JLabel textTrainName = new JLabel();
    JLabel textLocationName = new JLabel();
    JLabel textNextLocationName = new JLabel();
    JTextPane textStatus = new JTextPane();
    JLabel textPickUp = new JLabel(Bundle.getMessage("Pickup"));
    JLabel textInTrain = new JLabel(Bundle.getMessage("InTrain"));
    JLabel textSetOut = new JLabel(Bundle.getMessage("SetOut"));

    // panels
    JPanel pCars = new JPanel();

    public ShowCarsInTrainFrame() {
        super();
    }

    public void initComponents(Train train) {
        _train = train;

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JScrollPane carPane = new JScrollPane(pCars);
        carPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Cars")));
        carPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        // carPane.setPreferredSize(new Dimension(200, 300));

        // Set up the panels
        // Layout the panel by rows
        // row 2
        JPanel pRow2 = new JPanel();
        pRow2.setLayout(new BoxLayout(pRow2, BoxLayout.X_AXIS));

        // row 2a (train name)
        JPanel pTrainName = new JPanel();
        pTrainName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Train")));
        pTrainName.add(textTrainName);

        pRow2.add(pTrainName);

        // row 6
        JPanel pRow6 = new JPanel();
        pRow6.setLayout(new BoxLayout(pRow6, BoxLayout.X_AXIS));

        // row 10
        JPanel pRow10 = new JPanel();
        pRow10.setLayout(new BoxLayout(pRow10, BoxLayout.X_AXIS));

        // row 10a (location name)
        JPanel pLocationName = new JPanel();
        pLocationName.setBorder(BorderFactory.createTitledBorder("Location"));
        pLocationName.add(textLocationName);

        // row 10c (next location name)
        JPanel pNextLocationName = new JPanel();
        pNextLocationName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("NextLocation")));
        pNextLocationName.add(textNextLocationName);

        pRow10.add(pLocationName);
        pRow10.add(pNextLocationName);

        // row 12
        JPanel pRow12 = new JPanel();
        pRow12.setLayout(new BoxLayout(pRow12, BoxLayout.X_AXIS));

        pCars.setLayout(new GridBagLayout());
        pRow12.add(carPane);

        // row 13
        //  JPanel pStatus = new JPanel();
        //  pStatus.setLayout(new GridBagLayout());
        textStatus.setBorder(BorderFactory.createTitledBorder(""));
        //  addItem(pStatus, textStatus, 0, 0);
        textStatus.setBackground(null);
        textStatus.setEditable(false);

        getContentPane().add(pRow2);
        getContentPane().add(pRow6);
        getContentPane().add(pRow10);
        getContentPane().add(pRow12);
        getContentPane().add(textStatus);

        if (_train != null) {
            setTitle(Bundle.getMessage("TitleShowCarsInTrain", _train.getName()));

            // listen for train changes
            _train.addPropertyChangeListener(this);
        }

        // build menu
        JMenuBar menuBar = new JMenuBar();
        if (train != null) {
            JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
            toolMenu.add(new PrintShowCarsInTrainAction(false, train));
            toolMenu.add(new PrintShowCarsInTrainAction(true, train));
            menuBar.add(toolMenu);
        }
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_ShowCarsInTrain", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth300, Control.panelHeight500));
        update();
    }

    private void update() {
        log.debug("queue update");
        // use invokeLater to prevent deadlock
        SwingUtilities.invokeLater(() -> {
            log.debug("update");
            if (_train == null || _train.getRoute() == null) {
                return;
            }
            textTrainName.setText(_train.getIconName());
            pCars.removeAll();
            RouteLocation rl = _train.getCurrentRouteLocation();
            if (rl != null) {
                textLocationName.setText(trainManager.isShowLocationHyphenNameEnabled()
                        ? rl.getLocation().getName() : rl.getLocation().getSplitName());
                textNextLocationName.setText(trainManager.isShowLocationHyphenNameEnabled()
                        ? _train.getNextLocationName() : TrainCommon.splitString(_train.getNextLocationName()));
                // add header
                int i = 0;
                addItemLeft(pCars, textPickUp, 0, 0);
                addItemLeft(pCars, textInTrain, 1, 0);
                addItemLeft(pCars, textSetOut, 2, i++);
                // block cars by destination
                // caboose or FRED is placed at end of the train
                // passenger cars are already blocked in the car list
                // passenger cars with negative block numbers are placed at
                // the front of the train, positive numbers at the end of
                // the train.
                for (RouteLocation rld : _train.getRoute().getLocationsBySequenceList()) {
                    for (Car car : carManager.getByTrainDestinationList(_train)) {
                        if (TrainCommon.isNextCar(car, rl, rld, true)) {
                            log.debug("car ({}) routelocation ({}) track ({}) route destination ({})",
                                    car.toString(), car
                                            .getRouteLocation().getName(),
                                    car.getTrackName(), car.getRouteDestination().getName());
                            JCheckBox checkBox = new JCheckBox(car.getRoadName().split(TrainCommon.HYPHEN)[0] +
                                    " " +
                                    TrainCommon.splitString(car.getNumber()));
                            if (car.getRouteDestination() == rl) {
                                addItemLeft(pCars, checkBox, 2, i++); // set out
                            } else if (car.getRouteLocation() == rl && car.getTrack() != null) {
                                addItemLeft(pCars, checkBox, 0, i++); // pick up
                            } else {
                                addItemLeft(pCars, checkBox, 1, i++); // in
                                                                      // train
                            }
                        }
                    }
                }

                if (rl != _train.getTrainTerminatesRouteLocation()) {
                    textStatus.setText(getStatus(rl));
                } else {
                    textStatus.setText(MessageFormat.format(TrainManifestText.getStringTrainTerminates(),
                            new Object[]{_train.getTrainTerminatesName()}));
                }
            }
            pCars.repaint();
        });
    }

    private String getStatus(RouteLocation rl) {
        if (Setup.isPrintLoadsAndEmptiesEnabled()) {
            int emptyCars = _train.getNumberEmptyCarsInTrain(rl);
            return MessageFormat.format(TrainManifestText.getStringTrainDepartsLoads(), new Object[]{
                    rl.getSplitName(), rl.getTrainDirectionString(),
                    _train.getNumberCarsInTrain(rl) - emptyCars, emptyCars, _train.getTrainLength(rl),
                    Setup.getLengthUnit().toLowerCase(), _train.getTrainWeight(rl)});
        } else {
            return MessageFormat.format(TrainManifestText.getStringTrainDepartsCars(),
                    new Object[]{rl.getSplitName(), rl.getTrainDirectionString(), _train.getNumberCarsInTrain(),
                            _train.getTrainLength(rl), Setup.getLengthUnit().toLowerCase(),
                            _train.getTrainWeight(rl)});
        }
    }

    @Override
    public void dispose() {
        if (_train != null) {
            _train.removePropertyChangeListener(this);
        }
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // if (Control.showProperty)
        log.debug("Property change {} from: {} old: {} new: {}", e.getPropertyName(), e.getSource().toString(),
                e.getOldValue(), e.getNewValue()); // NOI18N
        if (e.getPropertyName().equals(Train.BUILT_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Train.TRAIN_MOVE_COMPLETE_CHANGED_PROPERTY)) {
            update();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ShowCarsInTrainFrame.class);
}
