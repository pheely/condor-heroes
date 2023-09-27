package main

import (
	"os"
	"net/http"
	"github.com/gorilla/mux"
	"github.com/rs/zerolog"
	"github.com/yfuruyama/crzerolog"

	"github.com/pheely/employee-api/internal/handler"
)

func CreateRouter(h handler.Service) *mux.Router {
	rootLogger := zerolog.New(os.Stdout)
	middleware := crzerolog.InjectLogger(&rootLogger)
	r := mux.NewRouter()
	r.Use(middleware)

	api := r.PathPrefix("/api").Subrouter()
	api.Use(handler.JsonHeader)
	// api.Use(h.SessionHandler)
	api.Methods(http.MethodOptions).HandlerFunc(
		func(w http.ResponseWriter, r *http.Request) {
			w.WriteHeader(http.StatusNoContent)
		})
	api.Path("/employee").Methods(http.MethodPost).HandlerFunc(h.CreateEmployee)
	api.Path("/employee").Methods(http.MethodGet).HandlerFunc(h.GetAllEmployees)
	api.Path("/employee").Methods(http.MethodDelete).HandlerFunc(h.DeleteAllEmployees)
	api.Path("/employee/{id}").Methods(http.MethodGet).HandlerFunc(h.GetEmployee)
	api.Path("/employee/{id}").Methods(http.MethodDelete).HandlerFunc(h.DeleteEmployee)
	api.Path("/employee/{id}").Methods(http.MethodPatch, http.MethodPut).HandlerFunc(h.UpdateEmployee)
	api.Path("/help").Methods(http.MethodGet, http.MethodGet).HandlerFunc(h.Help)

	return r
}
