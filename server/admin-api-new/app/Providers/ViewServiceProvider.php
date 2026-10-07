<?php
namespace App\Providers;


use Illuminate\Support\Facades\Blade;
use Illuminate\Support\ServiceProvider;

class ViewServiceProvider extends ServiceProvider
{
    public function boot()
    {
        // Enregistre le composant ui-error lié à la classe
        Blade::component('App\\View\\Components\\Ui\\Error', 'ui-error');
        Blade::component('App\\View\\Components\\Ui\\Input', 'ui-input');
        Blade::component('App\\View\\Components\\Ui\\Label', 'ui-label');
        Blade::component('App\\View\\Components\\Ui\\PasswordInput', 'ui-password-input');
    }
}
