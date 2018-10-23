FAQs
=======


Functionality
-------------

**Does the tool provide data-quality-check procedures?**

Waltz provides several mechanisms to improve data quality:
 
 - Attestations (currently supporting flows and ratings)
 - Administrative screens to identify orphan entries 
 - Custom Assessments, can be used programmatically to flag applications/change initiatives


**Is there a functionality to manage capabilities?**

Not currently.  A library of reusable code for managing measurable taxonomies is in use
at a customer site and may be included in the open source offering at a future date. 
  

**Is there a functionality to manage strategies (IT as well as business)?**

Waltz supports IT and business strategy via the following mechanisms:
 
- Roadmaps and Scenarios (new in 1.13)
- Current state ratings
- Change Initiatives
- Defining authoritative sources


**Is there any support for managing … ?**
e.g. IT-security & data-protection / Risk management / Demand management / IT-Procurement

A main focus of Waltz is to define the technical landscape in terms of custom taxonomies, 
assessments, ratings and other mechanisms.  Waltz is actively being used for documenting
sensitive dataflows, GDPR issues amongst other concerns.  It is less suited to Demand management and 
IT procurement. 


**Is there a functionality to support communication & collaboration?**                       

Not directly.  Waltz supports data-driven surveys (e.g. send a survey to all IT asset owners
of applications which handle financial product 'X').  In addition Waltz promotes a collaborative
approach to documenting the technical landscape by encouraging contributions from a wide range
of users. 

Waltz can be configured to send emails to inform users of events such as surveys or attestations
which may require their attention.
 

Security
--------

**What is covered by the security concept ?**
e.g. access-rights, roles, single-feature protection

Waltz implements a basic [role](https://github.com/khartec/waltz/blob/master/waltz-ng/client/user/roles.js) 
based security model.


**Is there single-sign-on supported (SAML)?**

SAML is not directly supported.  However Waltz can be configured with a pluggable 
security authentication layer as described in the [settings](https://github.com/khartec/waltz/blob/master/docs/features/configuration/settings.md#security)
documentation.


**How do you assess the future reliability/seurity?**

?


Non-Functionals
---------------

**How does the meta-model look like?**

The Waltz meta model is described in the 
[documentation](https://github.com/khartec/waltz/blob/master/docs/features/README.md)


**Which life-cycles for (which) objects are available, e.g. applications?**

There is a basic lifecycle 
(see the enum [EntityLifecycleStatus](https://github.com/khartec/waltz/blob/master/waltz-model/src/main/java/com/khartec/waltz/model/EntityLifecycleStatus.java))
supported by the main entities within Waltz. 
 
In addition some other classes of entities (e.g. Scenarios and Physical Specifications)
support and enhanced [ReleaseLifecycleStatus](https://github.com/khartec/waltz/blob/master/waltz-model/src/main/java/com/khartec/waltz/model/ReleaseLifecycleStatus.java)
which operates independently of the entity lifecycle.


Support
-------

**Is there a 3-year roadmap available?**

Not currently.


**Is the system optimized to run in a cloud?**

Not tested, though we have trialled it on Azure and are deploying it on an internal cloud at
a customer site.
