package com.example.demo;

import java.util.function.Function;

import com.example.demo.MockWeatherService.Request;
import com.example.demo.MockWeatherService.Response;

public class MockWeatherService implements Function<Request, Response> {

    public enum Unit {
        C, F
    }

    public record Request(String location, Unit unit) {
    }

    public record Response(double temp, Unit unit) {
    }

    public Response apply(Request request) {
        return new Response(30.0, Unit.C);
    }
}
