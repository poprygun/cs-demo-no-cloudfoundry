# Access Config Server (SCS Tile Cloudfoundry) from the Application that is NOT deployed to the Cloudfondry.

_Note that SSL validation is disabled - do not use in production_

## In case you want to get configuration from cli

```bash
cf install-plugin -r CF-Community "spring-cloud-services"
```

## Create Config Service


## You can customise the configuration repo if needed using config below.

It could serve the git directory structure that has properties for multiple projects in subfolders, thus `{application}` 

```json
{
  "git": {
    "uri": "https://github.com/poprygun/cs-demo-config",
    "searchPaths": "{application}"
  }
}
```

```bash
cf create-service p-config-server standard cs-demo-config-server -c '{"git": {"uri": "https://github.com/poprygun/cs-demo-config"}}'
cf update-service cs-demo-config-server -c ./config-server.json 
```

## Retrieve Config Server service credentials

```bash
cf service-key cs-demo-config-srvr config-server-key
Getting key config-server-key for service instance cs-demo-config-srvr as grricha...

{
 "access_token_uri": "https://p-spring-cloud-services.uaa.system.pcf.solipsys.com/oauth/token",
 "client_id": "p-config-server-48fff234-3721-4a54-a8e1-838f105bc78c",
 "client_secret": "xxxxxxxx",
 "uri": "https://config-0e817806-0219-45f0-ace2-c0c5a4683778.apps.pcf.solipsys.com"
}
```

- `Config Server` connection information is in `resources/lib/bootstrap.yml`

```yml
spring:
  application:
    name: alert-service
  cloud:
    config:
      uri: https://config-0e817806-0219-45f0-ace2-c0c5a4683778.apps.pcf.solipsys.com
      client:
        oauth2:
          clientId: p-config-server-48fff234-3721-4a54-a8e1-838f105bc78c
          clientSecret: xxxxxxxx
          accessTokenUri: https://p-spring-cloud-services.uaa.system.pcf.solipsys.com/oauth/token
      label: master
```

## To [get a token](https://gist.github.com/kelapure/1670b881b02cf3abe77891ea55d411fc)

```bash
http --body --form POST https://p-spring-cloud-services.uaa.run.pcfone.io/oauth/token grant_type=client_credentials --auth xexe-clientid:xexe-password | jq -r .access_token
```

## Use postman to debug config server response

provide token obtained from previous request
post to https://config-7307bf5b-d9a2-416f-a7d3-d8891aea26fa.apps.pcfone.io/demo-config-extra/cloud

