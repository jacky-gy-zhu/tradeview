#!/bin/bash

nohup java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8001 -jar tradeview-1.0.jar >tradeview.log &