[![official project](http://jb.gg/badges/official.svg)](https://github.com/JetBrains#jetbrains-on-github)

# BASE Exit categorization system

## Goals
- Make it easy for jumpers to find the slowest possible progression from less risky flights to more advanced flights with the possibility of never doing the advanced flights at all
- Provide categorization that allows for fine-grained, objective analysis of incident data
- Provide categorization that allows for assessing hypotheses on BASE incident data

## Non-goals
- Provide fine-grained distinction on the more advanced end of exit points

## Sensitive sites
- In the past there has been an instinct to tell folks that sensitive jumps are worse than they actually are
- We instead propose that exits where site access is threatened be labeled 'sensitive' and local regulating bodies issue guidelines that set higher standards for jumping at those locations even if they are technically labeled green or blue flights

## Hypotheses
- Flyers who have proven some base line of skill and follow recurrency protocols can sustainably repeat green and blue jumps for a lifetime
- Flights that offer lower margin yield a higher incident rate per jump.

## High-level design and guiding principles for categorization
The first version of code was written to allow for finer grain distinctions within blue and black categorizations. But we feel it is important to first get the algo working at a higher level with respect to the below prescriptions before iterating on finer grained distinctions

### Green
- This is the ideal exit for someone's first jumps, or for someone looking to regain currency
- Must have a clean, solid ledge
  - Feet are on level left to right
  - Large gear-up area that is mostly flat and allows one to walk up to put feet over the ledge without much balance
- Must have soft landing (ie not rock/road) landing that is at least 20m x 20m


### Blue
- A jumper should be able to safely recover from a minor mistake on any jump that is either green or blue
  - a slip
  - an offheading
  - linetwists
  - partial canopy collapse
  - tailwing bucked on exit
  - tension knot

### Black
- A black jump should be able to be safely completed in any low wind conditions regardless of wind direction or thermic activity.
- Black jumps require consistent performance; falling below the intermedian quartile of performance may result in severe injury or death.

### Red
- A red jump will result in severe injury or death if not completed with specific updraft or wind direction


# Other resources
* [Wingsuit exit rating system proposal document](https://docs.google.com/document/d/1o5gyGeIPlDgTJ5vPKI4J9P95pzmnM1hy93LcNmRVn9s/edit?tab=t.0)
* [Kotlin multiplatform library template](https://github.com/Kotlin/multiplatform-library-template)
* [Gradle Maven Publish Plugin \- Publishing to Maven Central](https://vanniktech.github.io/gradle-maven-publish-plugin/central/)
