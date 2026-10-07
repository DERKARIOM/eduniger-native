@extends('layouts.guest') 

@section('content')
<div class="min-h-screen flex items-center justify-center bg-[#00A06A] px-4 py-12">
    <div class="w-full max-w-md bg-white shadow-lg rounded-2xl p-8 space-y-6">
        <div class="text-center">
            <img src="{{ asset('images/edu niger test.svg') }}" alt="Logo" class="mx-auto w-20 h-20">
            <h2 class="mt-4 text-2xl font-bold text-gray-800">Bienvenue</h2>
            <p class="text-sm text-gray-500">Connectez-vous à votre compte</p>
        </div>

        <form method="POST" action="{{ route('login') }}" class="space-y-4">
            @csrf

            <div>
                <x-ui-label for="email">Email</x-ui-label>
                <x-ui-input name="email" type="email" placeholder="ex: nom@domaine.ne" required autofocus autocomplete="email">
                    <div name="icon">
                        <svg  class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" stroke-width="1.5"
                             viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round"
                                  d="M4.5 6.75h15v10.5h-15V6.75z" />
                        </svg>
                    </div>
                        
                   
                </x-ui-input>
                <x-ui-error field="email" />
            </div>

            <div>
                <x-ui-label for="password">Mot de passe</x-ui-label>
                <x-ui-password-input name="password" placeholder="********" required />
                <x-ui-error field="password" />
            </div>

            <div class="flex justify-end">
                <a href="{{ route('password.request') }}" class="text-sm text-indigo-600 hover:underline">
                    Mot de passe oublié ?
                </a>
            </div>

            <div>
                <button type="submit"
                        class="w-full py-2 px-4 bg-[#00A06A] hover:bg-[#008856] text-white font-semibold rounded-md shadow-sm transition duration-150">
                    Se connecter
                </button>
            </div>
        </form>

        <div class="text-center text-sm text-gray-600">
            Pas encore de compte ?
            <a href="{{ route('register') }}" class="text-[#00A06A] hover:underline font-semibold">
                S'inscrire
            </a>
        </div>
    </div>
</div>
@endsection
