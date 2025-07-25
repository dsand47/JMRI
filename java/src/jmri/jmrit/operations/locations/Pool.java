package jmri.jmrit.operations.locations;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.beans.Bean;

/**
 * Represents a pool of tracks that share their length.
 *
 * @author Daniel Boudreau Copyright (C) 2011
 * @author Gregory Madsen Copyright (C) 2012
 *
 */
public class Pool extends Bean {

    public static final String LISTCHANGE_CHANGED_PROPERTY = "poolListChange"; // NOI18N
    public static final String DISPOSE = "poolDispose"; // NOI18N

    private final static Logger log = LoggerFactory.getLogger(Pool.class);

    // stores tracks for this pool
    protected List<Track> _tracks = new ArrayList<>();

    protected String _id = "";

    public String getId() {
        return _id;
    }

    protected String _name = "";

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        String old = _name;
        _name = name;
        firePropertyChange("Name", old, name);
    }

    /**
     * The number of tracks in this pool.
     *
     * @return the number of tracks in this pool.
     */
    public int getSize() {
        return _tracks.size();
    }

    // for combo boxes
    @Override
    public String toString() {
        return _name;
    }

    public Pool(String id, String name) {
        super(false);
        log.debug("New pool ({}) id: {}", name, id);
        _name = name;
        _id = id;
    }

    public void dispose() {
        firePropertyChange(DISPOSE, null, DISPOSE);
    }

    /**
     * Adds a track to this pool
     *
     * @param track to be added.
     */
    public void add(Track track) {
        if (!_tracks.contains(track)) {
            int oldSize = _tracks.size();
            _tracks.add(track);
            firePropertyChange(LISTCHANGE_CHANGED_PROPERTY, oldSize, _tracks.size());
        }
    }

    /**
     * Removes a track from this pool
     *
     * @param track to be removed.
     */
    public void remove(Track track) {
        if (_tracks.contains(track)) {
            int oldSize = _tracks.size();
            _tracks.remove(track);
            firePropertyChange(LISTCHANGE_CHANGED_PROPERTY, oldSize, _tracks.size());
        }
    }

    public List<Track> getTracks() {
        // Return a copy to protect the internal list
        return new ArrayList<>(_tracks);
    }

    public int getTotalLengthTracks() {
        return getTracks().stream().map(track -> track.getLength()).reduce(0, Integer::sum);
    }
    
    /**
     * Used to determine the maximum available length for a given track
     * 
     * @param track the track being evaluated
     * @return maximum track length
     */
    public int getMaxLengthTrack(Track track) {
        int length = getTotalLengthTracks();
        for (Track t : getTracks()) {
            if (t == track) {
                continue;
            }
            length = length - t.getPoolMinimumLength();
        }
        return length;
    }

    /**
     * Request track length from one of the other tracks in this pool. Other
     * tracks in the same pool may have their length shortened or lengthened by
     * this operation.
     *
     * @param track  the track requesting additional length
     * @param length the length of rolling stock
     * @return true if successful
     */
    public boolean requestTrackLength(Track track, int length) {
        // is there a maximum length restriction?
        if (track.getUsedLength() + track.getReserved() + length > track.getPoolMaximumLength()) {
            return false;
        }
        // only request enough length for the rolling stock to fit
        int additionalLength = track.getUsedLength() + track.getReserved() + length - track.getLength();

        for (Track t : getTracks()) {
            // note that the reserved track length can be either positive or negative
            if (t != track) {
                if (t.getUsedLength() + t.getReserved() + additionalLength <= t.getLength()
                        && t.getLength() - additionalLength >= t.getPoolMinimumLength()) {
                    log.debug("Pool ({}) increasing track ({}) length ({}) decreasing ({})", getName(),
                            track.getName(), additionalLength, t.getName()); // NOI18N
                    t.setLength(t.getLength() - additionalLength);
                    track.setLength(track.getLength() + additionalLength);
                    return true;
                } else {
                    // steal whatever isn't being used by this track
                    int available = t.getLength() - (t.getUsedLength() + t.getReserved());
                    int min = t.getLength() - t.getPoolMinimumLength();
                    if (min < available) {
                        available = min;
                    }
                    if (available > 0) {
                        // adjust track lengths and reduce the additional length needed
                        log.debug("Pool ({}) incremental increase for track ({}) length ({}) decreasing ({})",
                                getName(), track.getName(), available, t.getName()); // NOI18N
                        t.setLength(t.getLength() - available);
                        track.setLength(track.getLength() + available);
                        additionalLength = additionalLength - available;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Used to determine if the option to use maximum track length in a pool is
     * enabled. Enabled when there's 3 or more tracks in the pool.
     * 
     * @return true if maximum track length option is available
     */
    public boolean isMaxLengthOptionEnabled() {
        // need 3 or more tracks in pool before allowing max track length option
        if (getSize() > 2) {
            return true;
        }
        return false;
    }

    public boolean isThereMaxLengthRestrictions() {
        for (Track t : getTracks()) {
            if (t.getPoolMaximumLength() != Integer.MAX_VALUE) {
                return true;
            }
        }
        return false;
    }
}
