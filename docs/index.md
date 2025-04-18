[![Discord](https://img.shields.io/discord/801211199592857672.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)](https://discord.gg/XPpFHhT8sk) ![GitHub downloads](https://img.shields.io/github/downloads/marvk/vatprism/total) ![GitHub Repo stars](https://img.shields.io/github/stars/marvk/vatprism?style=social)

## Download

#### Latest: {{ site.github.releases[0].name }} ({{ site.github.releases[0].published_at }})

[Changelog](https://github.com/marvk/vatprism/blob/master/CHANGELOG.md)

##### [Download Installer (Windows x64)]({{ site.github.releases[0].assets[2].browser_download_url }})

Note: Windows Defender SmartScreen might show [a warning](assets/images/warning.png) about the installer not being a
recognized app. This is harmless and simply an issue of the installer
being [unsigned.](https://docs.microsoft.com/en-us/windows/security/threat-protection/microsoft-defender-smartscreen/microsoft-defender-smartscreen-overview)

##### [Download Installer (macOS)]({{ site.github.releases[0].assets[1].browser_download_url }})

Note: Please read [this section of the readme](https://github.com/marvk/vatprism/#macos) in case you run into issues
with the macOS release of VATprism.

##### [Download Source]({{ site.github.zip_url }})

Note: Please read [this section of the readme](https://github.com/marvk/vatprism/#build) for instructions on how to
compile VATprism.

## Why VATprism?

VATprism has been build from the ground up to offer a no-fuss experience into the world of VATSIM data. VATprism makes
it easy to find Flights, Airports and other Data and navigate between that data. It offers fully customizable color
schemes, allowing you to adjust the interface colors to your liking. VATprism also allows you to create an unlimited
amount of filters, marking flights you are interested in with any color you prefer.

## Showcase

[![Overview](assets/images/showcase/overview.png)](assets/images/showcase/overview.png)

### Detail Views

VATprism allows you to simply click on any data displayed on the map: airports, flights, controllers and FIRs. This
brings up a detailed overview of the item that is easily navigable by clicking on any provided links.

#### Airports

[![Airport Detail View](assets/images/showcase/detail_airport.png)](assets/images/showcase/detail_airport.png)

See airport location, active controllers, metar, arrivals and departures!

#### Controllers

[![Controller Detail View](assets/images/showcase/detail_controller.png)](assets/images/showcase/detail_controller.png)

Displays controller information, location and their atis.

#### Pilots

[![Pilot Detail View](assets/images/showcase/detail_pilot.png)](assets/images/showcase/detail_pilot.png)

See a pilots' information, location including vertical speed and ETA, and flight plan including route and remarks and if
they are streaming on Twitch.

#### FIRs

[![Flight Information Region Detail View](assets/images/showcase/detail_fir.png)](assets/images/showcase/detail_fir.png)

Quickly check all controllers assigned to the FIR, including better FIR matching for controller callsigns with infixes,
such as EDDG-E and EDDG-P.

### Tables

VATprism includes a number of tables showing all kinds of network data. Pilots, Controllers, Airports, FIRs, UIRs and
more!

#### Airports

[![Airports Table](assets/images/showcase/table_airports.png)](assets/images/showcase/table_airports.png)

Show all airports with the number of incoming and outgoing flights and number of connected controllers.

#### Controllers

[![Controllers Table](assets/images/showcase/table_controllers.png)](assets/images/showcase/table_controllers.png)

See all controllers, including observers, with their location, rating and frequency.

#### Pilots

[![Pilots Table](assets/images/showcase/table_pilots.png)](assets/images/showcase/table_pilots.png)

Lists all connected pilots with all kinds of data, including ETA, ground speed and altitude. Want to find the fastest or
highest plane on the network? The longest flight? The flight that has been connected the longest? No problem!

#### Streamers

[![Streamers Table](assets/images/showcase/table_streamers.png)](assets/images/showcase/table_streamers.png)

Want to find a VATSIM stream to watch? This table lists all users that have included a link to their Twitch or YouTube 
stream in their remarks. [Want to make sure you show up here when you're streaming?](/streamers)

#### Distance Measure

<video autoplay loop controls>
  <source src="assets/images/showcase/distance_measure.mp4" type="video/mp4">
Your browser does not support the video tag.
</video> 

Measure distance between any two positions on the map, with an optional approximate duration calculated from an
adjustable ground speed.

### Filters

[![Filters View](assets/images/showcase/filters.png)](assets/images/showcase/filters.png)

Create an infinite amount of filters, filtering callsigns, CIDs, departure and arrival airports and much more, including
custom map colors. Filters are also disableable, and may be shared as a .json file.

### Full Text Search

[![Search View](assets/images/showcase/search.png)](assets/images/showcase/search.png)

VATprism supports full text search, making it easy to search for airport names, ICAO codes, client names and more.

### Color Schemes

VATprism allows you to customize every color drawn on the map individually. This gives you all the control you need to
customize the look of your VATprism map. Dark, light, colorful or muted, or anything in between. Anything is possible!

#### Longing for the tried and trusted?

[![VATspy Color Scheme](assets/images/showcase/color_scheme_vatspy.png)](assets/images/showcase/color_scheme_vatspy.png)

#### Dark

[![Dark Color Scheme](assets/images/showcase/color_scheme_dark.png)](assets/images/showcase/color_scheme_dark.png)

#### or Light?

[![Light Color Scheme](assets/images/showcase/color_scheme_light.png)](assets/images/showcase/color_scheme_light.png)

#### Basically Google Maps

[![Earth Color Scheme](assets/images/showcase/color_scheme_real.png)](assets/images/showcase/color_scheme_real.png)
