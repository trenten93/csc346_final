# CSC-346 Final Project

#### Crime Choropleth Map by Counties In the United States

This program uses crime data sourced from FBI crime reports to color a map of counties in the United states according 
to the crime rate for the county in 2016. First I built the database to contain the totals of all city law enforcement and county
law agency crime numbers. Second I loop through a map that contains the counties of all the states and 
I find the crime rate for each county and find the color to shade the county and apply that color to that county until
 this has been done for all counties. 
 
 Here are some images of the final product. 
 
 
 ##### Violent Crime Map: 
 ![alt text](https://i.imgur.com/fIUdUqE.jpg "Violent Crime Map")
 
 
 ##### Property Crime Map:
 ![alt text](https://i.imgur.com/YsGyOug.jpg "Property Crime Map")



##### Project Details:
All values are calculated for crimes per 1000 people. Brighter shades of red mean more crime for that county and lighter
shades of red mean less crime. There were 10 color steps used in this project and for the violent crime map the steps go from 
0 to 18+ crimes per 1000 people and the color steps every 2 additional crimes. 
The property crime map has a range of 0 to 90+ crimes and the color steps up with every 10 
additional crimes. All data including population was from 2016. 

The crime data was sourced from[FBI UCR](https://ucr.fbi.gov/crime-in-the-u.s/2016/crime-in-the-u.s.-2016/tables/table-6/table-6.xls/view)

I built a program that took the city crime data and placed those into county totals and then I 
added those to the county crime reports so the totals would include both city and county law agency reports. 

###### Other resources used in this project:

* For population: [Census.gov](https://www.census.gov/data/tables/2016/demo/popest/counties-total.html)

* For the city and county data: [simplemaps](https://simplemaps.com/data/us-cities)

