{{/*
Expand the name of the chart.
*/}}
{{- define "quarkus-playground.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "quarkus-playground.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- printf "%s" $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "quarkus-playground.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "quarkus-playground.labels" -}}
helm.sh/chart: {{ include "quarkus-playground.chart" . }}
{{ include "quarkus-playground.selectorLabels" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "quarkus-playground.selectorLabels" -}}
app: playground-app
app.kubernetes.io/name: {{ include "quarkus-playground.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Namespace name
*/}}
{{- define "quarkus-playground.namespace" -}}
{{- .Values.namespace.name }}
{{- end }}

{{/*
Create a connection hosts to monogo.
*/}}
{{- define "quarkus-playground.mongoHosts" -}}
{{- if .Values.mongo.sharding.enabled }}
{{- printf "%s-mongo-mongos.%s.svc.cluster.local:27017" .Release.Name .Values.namespace.name }}
{{- else }}
{{- printf "%s-mongo-rs0.%s.svc.cluster.local:27017" .Release.Name .Values.namespace.name }}
{{- end }}
{{- end }}

{{/*
Create a connection hosts to redis.
*/}}
{{- define "quarkus-playground.redisHosts" -}}
{{- printf "redis://%s-redis-cluster-headless.%s.svc.cluster.local:6379" .Release.Name .Values.namespace.name }}
{{- end }}

{{/*
Create a connection host to elasticsearch.
The service name follows the ECK convention: <fullnameOverride>-es-http.
*/}}
{{- define "quarkus-playground.elasticsearchHosts" -}}
{{- printf "elasticsearch-es-http.%s.svc.cluster.local:9200" .Values.namespace.name }}
{{- end }}