# CSE511-Project-Hotspot-Analysis-Template
#### Version history
v1.1, Nov 16, Fix a bug in "Entrace.scala"
v1.0, Nov 13, Initial version


## Requirement

In this phase, you are required to do spatial hot spot analysis. In particular, you need to complete two different hot spot analysis tasks 


### Hot zone analysis
HotZoneAnalysis(): To identify the hot zone, we are provided
with a list of rectangle(zone) coordinates and a list
of point coordinates. The input point data set represents
the pickup point of New York Taxi trip data sets. The
implementation returns true if a particular input point
lies inside the input rectangle(zone). The hotness of the
zone(rectangle) is based on the number of points inside
the zone.

### Hot cell analysis


We are given a list of pickup (x, y, z)
coordinates corresponding to latitude, longitude, and date
of pickup. By applying spatial statistics Getis-Ord, it is
required to determine the hot cells from the given input.
To apply Getis-ord, we first need the number of neighbors
of a cell. This is calculated based on the possible locations
of a cell, it can either be an edge, corner, face, or center
cell. The maximum neighbors would be 18 for the edge
cell, 8 for the corner, 27 for the center, and 12 for the
face cells. The next step would be to filter the given
data based on the bounded values (minX, maxX), (minY,
maxY), and (minZ, maxZ). Then count the number of
pickups at each pickup location(x, y, z) and store it in the
pickup table.


