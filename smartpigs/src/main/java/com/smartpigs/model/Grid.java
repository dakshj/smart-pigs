package com.smartpigs.model;

import java.util.List;

public class Grid {

    private final List<List<Occupant>> occupants;

    public Grid(final List<List<Occupant>> occupants) {
        this.occupants = occupants;
    }

    public List<List<Occupant>> getOccupants() {
        return occupants;
    }

    @Override
    public String toString() {
        final StringBuilder full = new StringBuilder();

        for (int row = 0; row < occupants.size(); row++) {
            final List<Occupant> rowList = occupants.get(row);
            final StringBuilder line = new StringBuilder();

            for (int col = 0; col < rowList.size(); col++) {
                final Occupant occupant = rowList.get(col);

                switch (occupant.getOccupantType()) {
                    case EMPTY:
                        line.append('E');
                        break;

                    case PIG:
                        line.append(occupant);
                        break;

                    case STONE:
                        line.append('S');
                        break;
                }

                if (col < rowList.size() - 1) {
                    line.append('\t');
                }
            }

            full.append(line);

            if (row < occupants.size() - 1) {
                full.append('\n');
            }
        }

        return full.toString();
    }
}
