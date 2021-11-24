# FaaS4FogSim

This repository contains the source code for the simulation software used within the papers **Towards Auction-Based Function Placement in Serverless Fog Platforms** and **AuctionWhisk: Using an Auction-Inspired Approach for Function Placement in Serverless Fog Platforms**

The Function-as-a-Service (FaaS) paradigm has a lot of potential as a computing model for fog environments comprising both cloud and edge nodes, as compute requests can be scheduled across the entire fog continuum in a fine-grained manner. When the request rate exceeds capacity limits at the resource-constrained edge, some functions need to be offloaded towards the cloud.
In this paper, we present an auction-inspired approach in which application developers bid on resources while fog nodes decide locally which functions to execute and which to offload in order to maximize revenue. Unlike many current approaches to function placement in the fog, our approach can work in an online and decentralized manner. We also present our proof-of-concept prototype AuctionWhisk that illustrates how such an approach can be implemented in a real FaaS platform. Through a number of simulation runs and system experiments, we show that revenue for overloaded nodes can be maximized without dropping function requests.

If you use this software in a publication, please cite it as:

### Text

David Bermbach, Setareh Maghsudi, Jonathan Hasenburg, Tobias Pfandzelter. **Towards Auction-Based Function Placement in Serverless Fog Platforms.** In: Proceedings of the Second IEEE International Conference on Fog Computing 2020 (ICFC 2020). IEEE 2020.
David Bermbach, Jonathan Bader, Jonathan Hasenburg, Tobias Pfandzelter, Lauritz Thamsen, **AuctionWhisk: Using an Auction-Inspired Approach for Function Placement in Serverless Fog Platforms**, Software: Practice and Experience, 2021.

### BibTeX
```
@inproceedings{paper_bermbach_auctions4function_placement,
	title = "Towards Auction-Based Function Placement in Serverless Fog Platforms",
	booktitle = "Proceedings of the Second {IEEE} {International} {Conference} on {Fog} {Computing} (ICFC 2020)",
	author = "Bermbach, David and Maghsudi, Setareh and Hasenburg, Jonathan and Pfandzelter, Tobias",
	year = 2020,
	publisher = "IEEE"
}

@article{bermbach-auctionwhisk-wiley,
    author = "Bermbach, David and Bader, Jonathan, and Hasenburg, Jonathan and Pfandzelter, Tobias and Thamsen, Lauritz",
    title = "AuctionWhisk: Using an Auction-Inspired Approach for Function Placement in Serverless Fog Platforms",
    journal = "Software: Practice and Experience",
    year = 2021,
    publisher = "Wiley"
}
```

For a full list of publications, please see [our website](https://www.mcc.tu-berlin.de/menue/forschung/publikationen/parameter/en/).

## Using the simulation

- Import the project in IntelliJ as Gradle Project
- Run MainKt and provide as argument:
    - `sim1`: study the effect of an increasing request load on the processing prices
    - `sim2`: study the effect of an increasing number of executables on storage prices
    - `sim3`: study the effect of node disloyalty (in the paper stickiness, 100% disloyal = 0% sticky)
- Parameter choices for sim1 and sim2 are detailed in `simResults/parameter_choices_paper_eval.txt`
- The simulation is deterministic, the results and some analysis results for sim1 and sim2 are also available in `simResults`
- You can customize simulation parameters in the CONFIG class
