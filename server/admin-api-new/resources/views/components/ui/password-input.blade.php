@props([
    'name' => 'password',
    'placeholder' => 'Mot de passe',
    'required' => false,
    'autofocus' => false,
    'autocomplete' => 'current-password',
])

@php
    $inputId = $attributes->get('id', $name);
@endphp

<div x-data="{ show: false }" class="relative">
    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
        <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" stroke-width="1.5"
             viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M12 15c1.656 0 3-1.567 3-3.5S13.656 8 12 8s-3 1.567-3 3.5S10.344 15 12 15z" />
            <path stroke-linecap="round" stroke-linejoin="round"
                  d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7-10-7-10-7z" />
        </svg>
    </div>

    <input
        :type="show ? 'text' : 'password'"
        id="{{ $inputId }}"
        name="{{ $name }}"
        placeholder="{{ $placeholder }}"
        autocomplete="{{ $autocomplete }}"
        {{ $required ? 'required' : '' }}
        {{ $autofocus ? 'autofocus' : '' }}
        {{ $attributes->merge([
            'class' => 'block w-full rounded-md border-gray-300 shadow-sm focus:border-[#00A06A] focus:ring-[#00A06A] sm:text-sm pl-10 pr-10'
        ]) }}
    >

    <button type="button"
            @click="show = !show"
            class="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-500"
            tabindex="-1">
        <template x-if="show">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none"
                 viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round"
                      d="M13.875 18.825A10.05 10.05 0 0112 19c-6 0-10-7-10-7a20.937 20.937 0 014.106-4.908m2.6-1.6A9.968 9.968 0 0112 5c6 0 10 7 10 7a20.897 20.897 0 01-2.455 3.137M9.879 9.879a3 3 0 014.242 4.242" />
                <path stroke-linecap="round" stroke-linejoin="round"
                      d="M3 3l18 18" />
            </svg>
        </template>
        <template x-if="!show">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none"
                 viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round"
                      d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                <path stroke-linecap="round" stroke-linejoin="round"
                      d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
            </svg>
        </template>
    </button>
</div>
