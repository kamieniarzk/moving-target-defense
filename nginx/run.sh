#!/bin/bash
docker run --name my-custom-nginx-container -v ~/IdeaProjects/spzc/nginx/conf/ubuntu.conf:/etc/nginx/nginx.conf:ro --network host -t -d nginx
